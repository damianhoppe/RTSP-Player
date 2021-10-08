package pl.huczeq.rtspplayer.data.expression;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Expression {

    private final String expression;
    private final ExpressionHelper<String> helper;
    private final List<String> partialyExpression;
    private final HashMap<String, String> rawVariables;

    public Expression(String expression, HashMap<String, String> rawVariables) {
        this.expression = expression;
        this.helper = new ExpressionHelper<>();
        this.partialyExpression = helper.splitExpression(this.expression, rawVariables);
        this.rawVariables = rawVariables;
    }

    public List<HashMap<String, Integer>> generatePatternDataList() {
        List<Variable> variables = new ArrayList<>();
        HashMap<Integer, Variable> variablesMap = new HashMap<>();
        helper.prepareVariableMaps(partialyExpression, rawVariables, variables, variablesMap);
        List<HashMap<String, Integer>> variations = Variations.generate(variables, -1);
        Log.d("TEST", "variations.size(): " + variations.size());
        return variations;
    }

    public List<HashMap<String, Integer>> generatePatternDataList(int limit) {
        List<Variable> variables = new ArrayList<>();
        HashMap<Integer, Variable> variablesMap = new HashMap<>();
        helper.prepareVariableMaps(partialyExpression, rawVariables, variables, variablesMap);
        List<HashMap<String, Integer>> variations = Variations.generate(variables, limit);
        Log.d("TEST", "variations.size(): " + variations.size());
        return variations;
    }

    public List<String> getPartialyExpression() {
        return this.partialyExpression;
    }

    public String getExpression() {
        return expression;
    }

    public ExpressionHelper<String> getHelper() {
        return helper;
    }

    public HashMap<String, String> getRawVariables() {
        return rawVariables;
    }
}
