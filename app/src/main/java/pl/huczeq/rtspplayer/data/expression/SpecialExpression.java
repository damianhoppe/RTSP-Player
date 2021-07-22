package pl.huczeq.rtspplayer.data.expression;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.exceptions.ParsingException;

public class SpecialExpression {

    public static final String variableDataRegex = "[0-9,\\-/]+";
    public static final String variableRegex = "\\{?([a-zA-z0-9]+=)?[0-9,\\-/]+\\}?";

    private String expression;
    private List<String> partialyExpression;
    private HashMap<String, String> variables;

    public SpecialExpression() {
    }

    public void initAdvanced(String expression) {
        this.expression = expression;
        this.partialyExpression = new ArrayList<>();
        this.variables = new HashMap<>();
        String tempExpression = this.expression;
        String buffer;
        String buffer2;
        int startIndex, endIndex, bufferIndex;
        while((startIndex = tempExpression.indexOf('{')) >= 0) {
            if(startIndex != 0) {
                partialyExpression.add(tempExpression.substring(0, startIndex));
                tempExpression = tempExpression.substring(startIndex);
            }
            endIndex = tempExpression.indexOf('}');
            buffer = tempExpression.substring(0, endIndex);
            bufferIndex = buffer.indexOf("=");
            if(buffer.length() <= 1) {
                throw new ParsingException(ParsingException.Error.DATA_EMPTY);
            }
            if(endIndex - bufferIndex == 1) {
                throw new ParsingException(ParsingException.Error.DATA_EXPECTED);
            }
            if(bufferIndex == -1) {
                if(!buffer.substring(1).matches(variableDataRegex)) {
                    throw new ParsingException(ParsingException.Error.DATA_EXPECTED, "" + (expression.length() - tempExpression.length()));
                }
            }else {
                if(!buffer.substring(bufferIndex+1).matches(variableDataRegex)) {
                    throw new ParsingException(ParsingException.Error.DATA_EXPECTED, "" + (expression.length() - tempExpression.length()));
                }
                buffer2 = buffer.substring(1, bufferIndex);
                if(this.variables.containsKey(buffer2))
                    throw new ParsingException(ParsingException.Error.NAME_IS_TAKEN, buffer2);
                variables.put(buffer2, buffer.substring(bufferIndex+1));
            }
            partialyExpression.add(buffer);
            tempExpression = tempExpression.substring(endIndex+1);
        }
        int tempIndex, tempVarIndex = 1;
        for(int i = 0; i < this.partialyExpression.size(); i++) {
            String stringValue = this.partialyExpression.get(i);
            if(stringValue.charAt(0) == '{') {
                tempIndex = stringValue.indexOf('=');
                if(tempIndex == -1) {
                    while(this.variables.containsKey("v" + tempVarIndex)) {
                        tempVarIndex++;
                    }
                    this.partialyExpression.set(i, "{v" + tempVarIndex + "=" + stringValue.substring(1));
                    variables.put("v" + tempVarIndex, stringValue.substring(1));
                }
            }
        }
        if(!tempExpression.isEmpty())
            partialyExpression.add(tempExpression);
    }

    public void initReader(String expression) {
        this.expression = expression;
        this.partialyExpression = new ArrayList<>();
        this.variables = new HashMap<>();
        String tempExpression = this.expression;
        String buffer;
        int startIndex, endIndex, bufferIndex, tempVarIndex = 0;
        while((startIndex = tempExpression.indexOf('{')) >= 0) {
            if(startIndex != 0) {
                partialyExpression.add(tempExpression.substring(0, startIndex));
                tempExpression = tempExpression.substring(startIndex);
            }
            endIndex = tempExpression.indexOf('}');
            buffer = tempExpression.substring(0, endIndex);
            bufferIndex = buffer.indexOf("=");
            if(buffer.length() <= 0) {
                throw new ParsingException(ParsingException.Error.EMPTY_EXPRESSION);
            }
            if(bufferIndex != -1) {
                buffer = buffer.substring(0, bufferIndex);
            }
            partialyExpression.add(buffer);
            System.out.println(buffer);
            tempExpression = tempExpression.substring(endIndex+1);
        }
        if(!tempExpression.isEmpty())
            partialyExpression.add(tempExpression);

        for(String msg : this.partialyExpression) {
            System.out.println(" - " + msg);
        }
    }

    public HashMap<String, String> getVariables() {
        return this.variables;
    }

    public String parse(HashMap<String, Integer> values) {
        Log.d("TEST", "Size: " + values.size());
        for(Map.Entry<String, Integer> entry : values.entrySet())
            Log.d("TEST", "Entry: " + entry.getKey() + " - " + entry.getValue());
        StringBuilder stringBuilder = new StringBuilder();
        String varName;
        int tempIndex;
        for(String partOfExpression : this.partialyExpression) {
            if(partOfExpression.startsWith("{")) {
                if((tempIndex = partOfExpression.indexOf('=')) == -1) {
                    varName = partOfExpression.substring(1);
                }else {
                    varName = partOfExpression.substring(1, partOfExpression.indexOf('='));
                }
                if(values.containsKey(varName)) {
                    stringBuilder.append(values.get(varName));
                }
            }else {
                stringBuilder.append(partOfExpression);
            }
        }
        return stringBuilder.toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(String value : partialyExpression) {
            stringBuilder.append(value);
            if(value.charAt(0) == '{') {
                stringBuilder.append('}');
            }
        }
        return stringBuilder.toString();
    }
}
