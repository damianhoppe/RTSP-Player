package pl.huczeq.rtspplayer.data.expression;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.exceptions.ParsingException;

public class Variable {

    private VariableModel model;
    private List<Integer> values;

    public Variable(String name, String value) {
        this(new VariableModel(name, value));
    }

    public Variable(VariableModel model) throws ParsingException{
        this.model = model;
        this.values = new ArrayList<>();
        try {
            parse(model.getValue());
        }catch (ParsingException e) {

        }
    }

    public VariableModel getModel() {
        return model;
    }

    public List<Integer> getValues() {
        return values;
    }

    public String getName() {
        return this.model.getName();
    }

    public void setName(String name) {
        this.model.setName(name);
    }

    public String getValue() {
        return this.model.getValue();
    }

    public void setValue(String value) {
        this.model.setValue(value);
    }

    public void parse() {
        parse(this.model.value);
    }

    private void parse(String data) {
        if(data.isEmpty())
            throw new ParsingException(ParsingException.Error.DATA_EMPTY);
        String[] dataUnits = data.split(",");
        for(String dataUnit : dataUnits) {
            int firstValue = 0;
            int secondValue = 0;
            if(dataUnit.contains("-")) {
                String[] dataArray = dataUnit.split("-");
                if(dataArray.length != 2)
                    throw new ParsingException(ParsingException.Error.NUMBER_INTERVAL_ARRAY_ERROR);
                try {
                    firstValue = Integer.parseInt(dataArray[0]);
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                try {
                    secondValue = Integer.parseInt(dataArray[1]);
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                if(firstValue < 0 || secondValue < 0) {
                    throw new ParsingException(ParsingException.Error.NEGATIVE_NUMBER_ERROR);
                }
                if(firstValue > secondValue)
                    throw new ParsingException(ParsingException.Error.NUMBER_INTERVAL_ORDER_ERROR);
                for(int i = firstValue; i <= secondValue; i++) {
                    addValue(i);
                }
            }else if(dataUnit.contains("/")) {
                String[] dataArray = dataUnit.split("/");
                if(dataArray.length != 2)
                    throw new ParsingException(ParsingException.Error.FIRST_LENGHT_ARRAY_ERROR);
                try {
                    firstValue = Integer.parseInt(dataArray[0]);
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                try {
                    secondValue = Integer.parseInt(dataArray[1]);
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                if(firstValue < 0 || secondValue < 0) {
                    throw new ParsingException(ParsingException.Error.NEGATIVE_NUMBER_ERROR);
                }
                for(int i = firstValue; i < firstValue+secondValue; i++) {
                    addValue(i);
                }
            }else {
                try {
                    firstValue = Integer.parseInt(dataUnit);
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                if(firstValue < 0) {
                    throw new ParsingException(ParsingException.Error.NEGATIVE_NUMBER_ERROR);
                }
                //if(!this.values.contains(firstValue)) this.values.add(firstValue);
                addValue(firstValue);
            }
        }
    }

    private void addValue(int value) {
        if(this.values.contains(value))
            return;
        /*
        if(this.values.size() > VariationsGenerator.MAX_VARIATIONS) {
            throw new ParsingException(ParsingException.Error.EXCEEDED_MAX_NUMBER_OF_VARIATIONS, this.model.getName());
        }*/
        this.values.add(value);
    }
}
