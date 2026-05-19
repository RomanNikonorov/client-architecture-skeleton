package me.nikonorov.clients.application.usecase;

import me.nikonorov.fanout.AsyncProperties;
import me.nikonorov.fanout.FanOutExecutor;
import me.nikonorov.clients.application.port.ExternalSystemAClient;
import me.nikonorov.clients.application.port.ExternalSystemBClient;
import me.nikonorov.clients.domain.ClientProfile;
import me.nikonorov.clients.domain.ClientProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Оркестрирует сценарий агрегации клиента.
 *
 * <p>Сценарий использования намеренно не зависит от транспорта. REST и gRPC
 * адаптеры маппят свои запросы в {@link ClientAggregationCommand}, затем этот класс выполняет
 * бизнес-оркестрацию: читает локальный профиль и собирает внешние сигналы через
 * прикладные порты.</p>
 *
 * <p>Параллельная работа делегируется в {@link FanOutExecutor}. Этот класс не
 * должен напрямую создавать virtual threads, semaphores или futures.</p>
 */
@Service
public class ClientAggregationUseCase {

    private final ClientProfileRepository profiles;
    private final ExternalSystemAClient systemA;
    private final ExternalSystemBClient systemB;
    private final FanOutExecutor fanOutExecutor;
    private final int maxParallelTasksPerRequest;

    /**
     * Создает сценарий агрегации со всеми необходимыми прикладными портами.
     *
     * @param profiles порт локального репозитория профилей клиентов
     * @param systemA исходящий порт для обязательной внешней системы A
     * @param systemB исходящий порт для внешней системы B
     * @param fanOutExecutor центральный ограниченный fan-out executor
     * @param asyncProperties конфигурация fan-out лимита
     */
    public ClientAggregationUseCase(
            ClientProfileRepository profiles,
            ExternalSystemAClient systemA,
            ExternalSystemBClient systemB,
            FanOutExecutor fanOutExecutor,
            AsyncProperties asyncProperties
    ) {
        this.profiles = profiles;
        this.systemA = systemA;
        this.systemB = systemB;
        this.fanOutExecutor = fanOutExecutor;
        this.maxParallelTasksPerRequest = asyncProperties.maxParallelTasksPerRequest();
    }

    /**
     * Агрегирует локальные и внешние данные для одного клиента.
     *
     * <p>Поиск профиля и оба внешних вызова отправляются в одну область fan-out,
     * поэтому настроенный лимит на один запрос управляет всей параллельной работой
     * для этой команды. Некритичные интеграции должны возвращать нормализованные
     * сигналы {@code UNAVAILABLE} из своих адаптеров; этот use case превращает
     * такие сигналы в предупреждения, видимые вызывающей стороне.</p>
     *
     * @param command провалидированная прикладная команда
     * @return агрегированные данные клиента и некритичные предупреждения
     */
    public ClientAggregationResult aggregate(ClientAggregationCommand command) {
        FanOutExecutor.FanOutScope fanOut = fanOutExecutor.openScope(maxParallelTasksPerRequest);

        FanOutExecutor.FanOutTask<ClientProfile> profileTask = fanOut.submit(
                () -> profiles.findByClientId(command.clientId()));
        FanOutExecutor.FanOutTask<ClientAggregationResult.ExternalSignal> systemATask = fanOut.submit(
                () -> systemA.getClientSignal(command));
        FanOutExecutor.FanOutTask<ClientAggregationResult.ExternalSignal> systemBTask = fanOut.submit(
                () -> systemB.getClientSignal(command));

        ClientProfile profile = profileTask.join();
        ClientAggregationResult.ExternalSignal signalA = systemATask.join();
        ClientAggregationResult.ExternalSignal signalB = systemBTask.join();

        List<String> warnings = new ArrayList<>();
        if ("UNAVAILABLE".equals(signalA.status())) {
            warnings.add("system-a unavailable");
        }
        if ("UNAVAILABLE".equals(signalB.status())) {
            warnings.add("system-b unavailable");
        }

        return new ClientAggregationResult(
                command.requestId(),
                command.clientId(),
                profile.segment(),
                profile.riskScore(),
                signalA,
                signalB,
                warnings
        );
    }
}
