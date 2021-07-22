package pl.huczeq.rtspplayer.domain.cameragenerator.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.domain.cameragenerator.exceptions.LimitReachedException;

public class Variations {

    public static List<Map<String, String>> generate(List<ProcessedVariable> variables, int limit) throws LimitReachedException {
        List<Map<String, String>> outVariations = new ArrayList<>();
        generate(outVariations, variables, new HashMap<>(), 0, limit);
        return outVariations;
    }

    private static void generate(List<Map<String, String>> outVariations, List<ProcessedVariable> variables, Map<String, String> currentPatternData, int variableIndex, int limit) throws LimitReachedException {
        if(Thread.currentThread().isInterrupted())
            throw new RuntimeException("Thread is interrupted");
        if(variableIndex >= variables.size()) {
            if(Thread.currentThread().isInterrupted())
                throw new RuntimeException("Thread is interrupted");
            outVariations.add(new HashMap<>(currentPatternData));
            if(limit > 0 && outVariations.size() >= limit) {
                throw new LimitReachedException();
            }
            return;
        }
        String tempString = null;
        ProcessedVariable variable = variables.get(variableIndex);
        variableIndex++;
        for(String value : variable.getValues()) {
            if(Thread.currentThread().isInterrupted())
                throw new RuntimeException("Thread is interrupted");
            currentPatternData.put(variable.getName(), value);
            generate(outVariations, variables, currentPatternData, variableIndex, limit);
        }
    }
}
