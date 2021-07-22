package pl.huczeq.rtspplayer.data.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Variations {

    public static List<HashMap<String, Integer>> generate(List<Variable> variables) {
        List<HashMap<String, Integer>> outVariations = new ArrayList<>();
        generate(outVariations, variables, new HashMap<>(), 0);
        return outVariations;
    }

    private static void generate(List<HashMap<String, Integer>> outVariations, List<Variable> variables, HashMap<String, Integer> currentPatternData, int variableIndex) {
        if(variableIndex >= variables.size()) {
            outVariations.add(new HashMap<>(currentPatternData));
            return;
        }
        String tempString = null;
        Variable variable = variables.get(variableIndex);
        variableIndex++;
        for(Integer value : variable.getValues()) {
            currentPatternData.put(variable.getName(), value);
            generate(outVariations, variables, currentPatternData, variableIndex);
        }
    }
}
