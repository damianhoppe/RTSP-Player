package pl.huczeq.rtspplayer.domain.cameragenerator.expression;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionHelper<T> {

    /**
     * Split expression by expressions {}, but and only when variable exists in variables Map
     * @param expression
     * @param variables
     * @return
     */
    public List<String> splitExpression(String expression, Map<String, T> variables, List<String> specialNames) {
        List<String> partialyExpression = new ArrayList<>();
        if(Strings.isNullOrEmpty(expression))
            return partialyExpression;
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
            partialyExpression.add(expression);
        return partialyExpression;
    }

    /**
     * Convert variables to List<Variable> and Map<Integer(index of variable in partialyExpression), Variable>
     * @param partialyExpression
     * @param variables
     * @param outAllVariables
     * @param outVariablesMap
     */
    public void prepareVariableMaps(List<String> partialyExpression, Map<String, String> variables, List<ProcessedVariable> outAllVariables, Map<Integer, ProcessedVariable> outVariablesMap) {
        Map<String, ProcessedVariable> tempVariableNameMap = new HashMap<>();
        for(int i = 0; i < partialyExpression.size(); i++) {
            String expressionPart = partialyExpression.get(i);
            if(expressionPart.startsWith("{")) {
                expressionPart = expressionPart.substring(1);
                if(variables.containsKey(expressionPart)) {
                    ProcessedVariable variable = tempVariableNameMap.get(expressionPart);
                    if(variable == null) {
                        variable = new ProcessedVariable(expressionPart, variables.get(expressionPart));
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

    public static String loadDataToExpression(List<String> partiallyExpression, Map<String, String> patterData) {
        StringBuilder stringBuilder = new StringBuilder();
        String varName;
        for(String partOfExpression : partiallyExpression) {
            if(partOfExpression.startsWith("{")) {
                varName = partOfExpression.substring(1);
                if(patterData.containsKey(varName)) {
                    stringBuilder.append(patterData.get(varName));
                }
            }else {
                stringBuilder.append(partOfExpression);
            }
        }
        return stringBuilder.toString();
    }

    public static String loadDataToExpression(String expression, Map<String, String> patterData, List<String> specialNames) {
        ExpressionHelper<String> helper = new ExpressionHelper<>();
        List<String> partiallyExpression = helper.splitExpression(expression, patterData, specialNames);
        return loadDataToExpression(partiallyExpression, patterData);
    }
}
