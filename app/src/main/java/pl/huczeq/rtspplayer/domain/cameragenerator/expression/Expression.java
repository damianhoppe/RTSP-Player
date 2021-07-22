package pl.huczeq.rtspplayer.domain.cameragenerator.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expression {

    private final String expression;
    private final ExpressionHelper<String> helper;
    private final List<String> partiallyExpression;
    private final Map<String, String> rawVariables;

    private final List<String> specialNames;

    /**
     * @param expression   Input text that may contain variables
     * @param rawVariables Map of variables; key - name of the variable, value - raw text of the variable value
     * @param specialNames
     */
    public Expression(String expression, Map<String, String> rawVariables, List<String> specialNames) {
        this.expression = expression;
        this.helper = new ExpressionHelper<>();
        //Split an expression into a list to find variables faster
        //Instead of searching character by character, if the first character of the element is '{' - variable
        this.partiallyExpression = helper.splitExpression(this.expression, rawVariables, specialNames);
        this.rawVariables = rawVariables;
        this.specialNames = specialNames;
    }

    public Expression(String expression, Map<String, String> rawVariables) {
        this(expression, rawVariables, List.of());
    }


    public List<Map<String, String>> generateVariations() {
        List<ProcessedVariable> variables = new ArrayList<>();
        Map<Integer, ProcessedVariable> variablesMap = new HashMap<>();
        //Prepare data; partiallyExpression and rawVariables ->> variables and variablesMap
        helper.prepareVariableMaps(partiallyExpression, rawVariables, variables, variablesMap);

        //Generate variations
        List<Map<String, String>> variations = Variations.generate(variables, -1);
        return variations;
    }

    public List<String> getPartiallyExpression() {
        return this.partiallyExpression;
    }
}
