package pl.huczeq.rtspplayer.data.expression;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class ExpressionParser {

    public static String loadDataToExpression(List<String> partialyExpression, HashMap<String, Integer> patterData) {
        StringBuilder stringBuilder = new StringBuilder();
        String varName;
        for(String partOfExpression : partialyExpression) {
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

    public static String loadDataToExpression(String expression, HashMap<String, Integer> patterData) {
        ExpressionHelper<Integer> helper = new ExpressionHelper<>();
        List<String> partialyExpression = helper.splitExpression(expression, patterData);
        StringBuilder stringBuilder = new StringBuilder();
        String varName;
        for(String partOfExpression : partialyExpression) {
            Log.d("Test", "loop: " + partOfExpression);
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
}
