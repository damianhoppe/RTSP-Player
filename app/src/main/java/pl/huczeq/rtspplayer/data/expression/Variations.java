package pl.huczeq.rtspplayer.data.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.huczeq.rtspplayer.exceptions.LimitReachedException;

public class Variations {

    public static List<HashMap<String, Integer>> generate(List<Variable> variables, int limit) {
        List<HashMap<String, Integer>> outVariations = new ArrayList<>();
        try {
            generate(outVariations, variables, new HashMap<>(), 0, limit);
        } catch (LimitReachedException e) {
            e.printStackTrace();
        }
        return outVariations;
    }

    private static void generate(List<HashMap<String, Integer>> outVariations, List<Variable> variables, HashMap<String, Integer> currentPatternData, int variableIndex, int limit) throws LimitReachedException {
        if(variableIndex >= variables.size()) {
            outVariations.add(new HashMap<>(currentPatternData));
            if(limit > 0 && outVariations.size() >= limit) {
                throw new LimitReachedException();
            }
            return;
        }
        String tempString = null;
        Variable variable = variables.get(variableIndex);
        variableIndex++;
        for(Integer value : variable.getValues()) {
            currentPatternData.put(variable.getName(), value);
            generate(outVariations, variables, currentPatternData, variableIndex, limit);
        }
    }
}
