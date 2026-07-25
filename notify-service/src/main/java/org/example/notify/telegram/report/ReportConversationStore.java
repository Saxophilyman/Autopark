package org.example.notify.telegram.report;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReportConversationStore {

    public enum Step { WAITING_VEHICLE, WAITING_PERIOD, WAITING_FROM, WAITING_TO }

    @Getter @Setter
    public static class State {
        private Step step;
        private String plate;
        private ReportPeriod period;
        private LocalDate from;
        private LocalDate to;

        public State() {
            this.step = Step.WAITING_VEHICLE;
        }
    }

    private final ConcurrentHashMap<Long, State> states = new ConcurrentHashMap<>();

    public void start(long chatId) {
        states.put(chatId, new State());
    }

    public Optional<State> get(long chatId) {
        return Optional.ofNullable(states.get(chatId));
    }

    public boolean isActive(long chatId) {
        return states.containsKey(chatId);
    }

    public void clear(long chatId) {
        states.remove(chatId);
    }
}
