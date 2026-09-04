package experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gestionnaire d'exécution et registre central de toutes les expériences menées au sein du laboratoire.
 * Maintient l'historique et assure la synchronisation thread-safe avec l'IHM Swing.
 */
public class ExperimentRunner {

    private static final ExperimentRunner INSTANCE = new ExperimentRunner();

    private final List<ExperimentResult> history = new CopyOnWriteArrayList<>();
    private final List<HistoryChangeListener> listeners = new CopyOnWriteArrayList<>();

    public interface HistoryChangeListener {
        void onHistoryUpdated(List<ExperimentResult> updatedHistory);
    }

    private ExperimentRunner() {
    }

    public static ExperimentRunner getInstance() {
        return INSTANCE;
    }

    public void addResult(ExperimentResult result) {
        if (result != null) {
            history.add(0, result); // Le plus récent en premier
            notifyListeners();
        }
    }

    public void addResults(List<ExperimentResult> results) {
        if (results != null && !results.isEmpty()) {
            history.addAll(0, results);
            notifyListeners();
        }
    }

    public List<ExperimentResult> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
        notifyListeners();
    }

    public void addListener(HistoryChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(HistoryChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<ExperimentResult> unmodifiable = Collections.unmodifiableList(history);
        for (HistoryChangeListener l : listeners) {
            l.onHistoryUpdated(unmodifiable);
        }
    }
}
