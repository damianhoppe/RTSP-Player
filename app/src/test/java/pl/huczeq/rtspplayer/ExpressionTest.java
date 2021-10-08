package pl.huczeq.rtspplayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.data.expression.ExpressionHelper;
import pl.huczeq.rtspplayer.data.expression.Variable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionTest {

    @Test
    public void expressionHelper_isCorrect_test() {
        ExpressionHelper helper = new ExpressionHelper();
        expressionHelper_isCorrect_testHelper(helper, "elo{var1}xd2{var2xd}elo2", true);
        expressionHelper_isCorrect_testHelper(helper, "elo{{var1}xd2{var2xd}elo2", false);
    }

    public void expressionHelper_isCorrect_testHelper(ExpressionHelper expressionHelper, String expression, boolean expectedResult) {
        assertEquals(expressionHelper.isCorrect(expression), expectedResult);
    }

    @Test
    public void expressionHelper_splitExpression_test() {
        ExpressionHelper helper = new ExpressionHelper();
        HashMap<String, String> vars = new HashMap<>();
        expressionHelper_splitExpression_testHelper(helper, "{var1", vars, new String[]{"{var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1}xd2{var2xd}elo2", vars, new String[]{"elo{var1}xd2{var2xd}elo2"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1", vars, new String[]{"elo{var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo}var1", vars, new String[]{"elo}var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo{}var1", vars, new String[]{"elo{}var1"});
        expressionHelper_splitExpression_testHelper(helper, "", vars, new String[]{""});

        vars.put("var1", "value");
        expressionHelper_splitExpression_testHelper(helper, "var1{i}", vars, new String[]{"var1","{i"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1}xd2{var2xd}elo2", vars, new String[]{"elo","{var1","xd2{var2xd}elo2"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1", vars, new String[]{"elo{var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo}var1", vars, new String[]{"elo}var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo{}var1", vars, new String[]{"elo{}var1"});
        expressionHelper_splitExpression_testHelper(helper, "", vars, new String[]{""});
    }

    private void expressionHelper_splitExpression_testHelper(ExpressionHelper helper, String expression, HashMap<String, String> variables, String[] expectedParts) {
        List<String> parts = helper.splitExpression(expression, variables);
        assertArrayEquals(expectedParts, parts.toArray());
    }

    @Test
    public void expressionHelper_generateVariablesMap_test() {
        ExpressionHelper helper = new ExpressionHelper();
        HashMap<String, String> vars = new HashMap<>();
        vars.put("var1", "1");
        expressionHelper_generateVariablesMap_testHelper(helper, "test{var1}xd{var1}", vars);
    }

    private void expressionHelper_generateVariablesMap_testHelper(ExpressionHelper helper, String expression, HashMap<String, String> variables) {
        List<Variable> allVariables = new ArrayList<>();
        HashMap<Integer, Variable> variablesMap = new HashMap<>();
        helper.prepareVariableMaps(helper.splitExpression(expression, variables), variables, allVariables, variablesMap);
        assertEquals(allVariables.size(), 1);
        assertEquals(variablesMap.size(), 2);
        int i = 1;
        for(Map.Entry<Integer,Variable> entry : variablesMap.entrySet()) {
            assertTrue(entry.getKey() == i);
            assertTrue(entry.getValue().getName().equals("var1"));
            assertTrue(entry.getValue() == allVariables.get(0));
            i += 2;
        }
    }
}
