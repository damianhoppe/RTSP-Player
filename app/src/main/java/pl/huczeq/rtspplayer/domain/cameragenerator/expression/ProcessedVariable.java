package pl.huczeq.rtspplayer.domain.cameragenerator.expression;

import org.jetbrains.annotations.TestOnly;

import java.util.List;

import pl.huczeq.rtspplayer.domain.cameragenerator.exceptions.ParsingException;

public class ProcessedVariable {

    private VariableModel model;
    private List<String> values;


    public ProcessedVariable(String name, String value) {
        this(new VariableModel(name, value));
    }

    public ProcessedVariable(VariableModel model) throws ParsingException{
        this.model = model;
        try {
            parse();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
    }

    public VariableModel getModel() {
        return model;
    }

    public List<String> getValues() {
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

    @TestOnly
    public void parse() {
        this.values = NumberParser.parse(this.model.value);
    }
}
