package pl.huczeq.rtspplayer.domain.cameragenerator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.domain.cameragenerator.expression.ProcessedVariable;
import pl.huczeq.rtspplayer.domain.cameragenerator.expression.Variations;
import pl.huczeq.rtspplayer.domain.cameragenerator.exceptions.LimitReachedException;

public class VariationsTest {

    List<ProcessedVariable> variables;
    List<Map<String, String>> variations;
    int expectedVariationsSize;

    @Before
    public void generateVariations() {
        variables = new ArrayList<>();
        variables.add(new ProcessedVariable("par1", "1,2"));
        variables.add(new ProcessedVariable("par2", "8-9"));
        variables.add(new ProcessedVariable("par3", "12/2"));

        expectedVariationsSize = 8;

        variations = Variations.generate(variables, -1);
    }

    @Test
    public void generating_Should_GenerateVariationsSize_Correctly() {
        assertEquals(expectedVariationsSize, variations.size());
    }

    @Test
    public void generating_Should_GenerateEveryVariatonSize_Correctly() {
        for(int i = 0; i < variations.size(); i++)
            assertEquals(variables.size(), variations.get(i).size());
    }

    @Test
    public void generating_Should_GeneratedEveryVariatonsNamesCorrectly_BasedOnVariableNames() {
        for(int i = 0; i < variations.size(); i++) {
            Map<String, String> variation = variations.get(i);
            for(Map.Entry<String, String> variationVariable : variation.entrySet()) {
                boolean variableNameExists = false;
                for(ProcessedVariable variable : variables) {
                    if(variable.getName().equals(variationVariable.getKey())) {
                        variableNameExists = true;
                        break;
                    }
                }
                assertTrue(variableNameExists);
            }
        }
    }

    @Test
    public void generating_Should_GeneratedEveryVariatonsCorrectly_BasedOnVariableValue() {
        List<String> variableNames = List.of("par1", "par2", "par3");
        List<Map<String, String>> expectedVariations = new ArrayList<>();
        expectedVariations.add(buildVariation(variableNames, List.of("1", "8", "12")));
        expectedVariations.add(buildVariation(variableNames, List.of("1", "8", "13")));
        expectedVariations.add(buildVariation(variableNames, List.of("1", "9", "12")));
        expectedVariations.add(buildVariation(variableNames, List.of("1", "9", "13")));
        expectedVariations.add(buildVariation(variableNames, List.of("2", "8", "12")));
        expectedVariations.add(buildVariation(variableNames, List.of("2", "8", "13")));
        expectedVariations.add(buildVariation(variableNames, List.of("2", "9", "12")));
        expectedVariations.add(buildVariation(variableNames, List.of("2", "9", "13")));

        for(Map<String, String> expectedVariation : expectedVariations)
            assertTrue(containsVariation(variations, expectedVariation));
    }

    private boolean containsVariation(List<Map<String, String>> list, Map<String, String> variation) {
        for(Map<String, String> variationFromList : list) {
            if(variationFromList.equals(variation))
                return true;
        }
        return false;
    }

    private Map<String, String> buildVariation(List<String> keys, List<String> values) {
        Map<String, String> variation = new HashMap<>();
        for(int i = 0; i < keys.size(); i++) {
            variation.put(keys.get(i), values.get(i));
        }
        return variation;
    }

    @Test
    public void generating_Should_ThrowLimitReachedException_When_NumberOfValuesExceededLimit() {
        List<ProcessedVariable> variables = new ArrayList<>();
        variables.add(new ProcessedVariable("par1", "1-100"));

        assertThrows(LimitReachedException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Variations.generate(variables, 2);
            }
        });
    }
}
