package pl.huczeq.rtspplayer.data.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.exceptions.ParsingException;

public class VariationsGenerator {

    public static final int MAX_VARIATIONS = 64;

    private List<Variable> collection;
    private List<HashMap<String, Integer>> variations;
    private SpecialExpression expression;

    private int size;

    public VariationsGenerator() {
    }

    private void cleanUp() {
        this.collection = new ArrayList<>();
        this.variations = new ArrayList<>();
        this.size = 0;
    }

    public void init(String stringValue) {
        cleanUp();
        /*
        if(stringValue.contains(" ")) {
            throw new ParsingException(ParsingException.Error.DATA_INCORRECT);
        }

        int index = 0;
        boolean readingExpression = false;
        String expressionBuff = "";
        while(index < stringValue.length()) {
            char currentChar = stringValue.charAt(index);
            if(!readingExpression) {
                if(currentChar == '{') {
                    readingExpression = true;
                    this.partialyInputValue.add(expressionBuff);
                    expressionBuff = "";
                }else {
                    expressionBuff += currentChar;
                }
            }else {
                if(currentChar == '}') {
                    readingExpression = false;
                    if(expressionBuff.contains("=")) {
                        String[] expressionData = expressionBuff.split("=");
                        if(expressionData.length != 2)
                            throw new ParsingException(ParsingException.Error.DATA_INCORRECT);
                        expressionData[0] = expressionData[0].trim();
                        if(expressionData[0].length() == 0)
                            expressionData[0] = null;
                        if(expressionData.length >= 32)
                            throw new ParsingException(ParsingException.Error.NAME_TOO_LONG, expressionData[0]);
                        Variable element = new Variable(expressionData[0], expressionData[1].trim());
                        addElement(element);
                    }else {
                        Variable element = new Variable(null, expressionBuff.trim());
                        addElement(element);
                    }
                    expressionBuff = "";
                }else {
                    expressionBuff += currentChar;
                }
            }
            index++;
        }
        partialyInputValue.add(expressionBuff);


        index = 0;
        int varIndex = 0;
        for(Variable element : this.collection) {
            if(element.getName() != null) {
                for(String specialNamePrefix : this.specialNamePrefixes) {
                    if(element.getName().startsWith(specialNamePrefix))
                        throw new ParsingException(ParsingException.Error.NAME_PREFIX_FORBIDDEN, specialNamePrefix);
                }
                for(String specialName: this.specialNames) {
                    if (element.getName().equalsIgnoreCase(specialName))
                        throw new ParsingException(ParsingException.Error.NAME_FORBIDDEN, specialName);
                }
            }
            if(element.getName() == null) {
                element.setName("var" + varIndex);
                varIndex++;
            }
            this.partialyInputValue.add(index*2 + 1, "{" + element.getName());
            index++;
        }
*/
        this.expression = new SpecialExpression();
        expression.initAdvanced(stringValue);
        for(Map.Entry<String, String> variableData : expression.getVariables().entrySet()) {
            Variable variable = new Variable(variableData.getKey(), variableData.getValue());
            this.collection.add(variable);
            if(this.size == 0)
                this.size += variable.getValues().size();
            else
                this.size *= variable.getValues().size();
        }
        if(this.size == 0) {
            this.size = 1;
            this.variations.add(new HashMap<>());
        } else {
            generateVariations();
        }
    }

    private void generateVariations() {
        generateVariations(new HashMap<>(), 0);
    }

    private void generateVariations(HashMap<String, Integer> currentData, int index) {
        if(index >= this.collection.size()) {
            this.variations.add(new HashMap<>(currentData));
            return;
        }
        String tempString = null;
        Variable variable = this.collection.get(index);
        index++;
        for(Integer value : variable.getValues()) {
            currentData.put(variable.getName(), value);
            generateVariations(currentData, index);
        }
    }

    public List<Variable> getCollection() {
        return this.collection;
    }

    public List<HashMap<String, Integer>> getVariations() {
        return this.variations;
    }

    public int size() {
        return this.size;
        /*
        if(this.collection.isEmpty())
            return 0;
        int size = 0;
        for(IntElement element : this.collection) {
            if(size == 0)
                size += element.getValues().size();
            else
                size *= element.getValues().size();
        }
        return size;*/
    }

    private void addElement(Variable element) {
        if(this.size == 0)
            this.size = element.getValues().size();
        else
            this.size *= element.getValues().size();
        if(this.size > MAX_VARIATIONS)
            throw new ParsingException(ParsingException.Error.EXCEEDED_MAX_NUMBER_OF_VARIATIONS, element.getName());
        this.collection.add(element);
    }

    public SpecialExpression getExpression() {
        return this.expression;
    }
}
