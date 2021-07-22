package pl.huczeq.rtspplayer.data.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpressionHelper<T> {

    private List<String> specialNames;

    public ExpressionHelper() {
        this.specialNames = new ArrayList<>();
        this.specialNames.add("index");
        this.specialNames.add("i");
    }

    public boolean isCorrect(String expression) {
        return true;
    }

    public List<String> splitExpression(String expression, HashMap<String, T> variables) {
        List<String> partialyExpression = new ArrayList<>();
        String tempExpression = expression;
        String buffer;
        int startIndex, endIndex;
        while((startIndex = tempExpression.indexOf('{')) >= 0) {
            if(startIndex != 0) {
                partialyExpression.add(tempExpression.substring(0, startIndex));
                tempExpression = tempExpression.substring(startIndex);
            }
            endIndex = tempExpression.indexOf('}');
            if(endIndex < 0) {
                addOrConnectAtLastIndex(partialyExpression, tempExpression);
                tempExpression = "";
                break;
            }
            buffer = tempExpression.substring(0, endIndex);
            partialyExpression.add(buffer);
            tempExpression = tempExpression.substring(endIndex+1);
        }
        if(!tempExpression.isEmpty())
            partialyExpression.add(tempExpression);
        for(int i = 1; i < partialyExpression.size(); i += 2) {
            String str = partialyExpression.get(i).substring(1);
            if(!variables.containsKey(str) && !specialNames.contains(str)) {
                partialyExpression.set(i-1, partialyExpression.get(i-1) + partialyExpression.get(i) + "}" + safeGetFromList(partialyExpression, i+1));
                if(i < partialyExpression.size())
                    partialyExpression.remove(i);
                if(i < partialyExpression.size())
                    partialyExpression.remove(i);
                i -= 2;
            }
        }
        if(partialyExpression.size() == 0)
            partialyExpression.add("");
        return partialyExpression;
    }

    public void prepareVariableMaps(List<String> partialyExpression, HashMap<String, String> variables, List<Variable> outAllVariables, HashMap<Integer, Variable> outVariablesMap) {
        HashMap<String, Variable> tempVariableNameMap = new HashMap<>();
        for(int i = 0; i < partialyExpression.size(); i++) {
            String expressionPart = partialyExpression.get(i);
            if(expressionPart.startsWith("{")) {
                expressionPart = expressionPart.substring(1);
                if(variables.containsKey(expressionPart)) {
                    Variable variable = tempVariableNameMap.get(expressionPart);
                    if(variable == null) {
                        variable = new Variable(expressionPart, variables.get(expressionPart));
                        tempVariableNameMap.put(expressionPart, variable);
                    }
                    outVariablesMap.put(i ,variable);
                }
            }
        }
        outAllVariables.addAll(tempVariableNameMap.values());
    }

    private static void addOrConnectAtLastIndex(List<String> list, String data) {
        if(list.size() == 0) {
            list.add(data);
        }else {
            int lastIndex = list.size() - 1;
            list.set(lastIndex, list.get(lastIndex) + data);
        }
    }

    private static String safeGetFromList(List<String> list, int index) {
        if(index < list.size())
            return list.get(index);
        return "";
    }
}
