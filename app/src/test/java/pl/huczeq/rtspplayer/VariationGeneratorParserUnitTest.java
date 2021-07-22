package pl.huczeq.rtspplayer;

import org.junit.Test;

import pl.huczeq.rtspplayer.data.expression.SpecialExpression;
import pl.huczeq.rtspplayer.data.expression.VariationsGenerator;
import pl.huczeq.rtspplayer.exceptions.ParsingException;

import static org.junit.Assert.*;

public class VariationGeneratorParserUnitTest {

    @Test
    public void test() {
        SpecialExpression expression = new SpecialExpression();
        try {
            expression.initReader("siemka{elo=1}xd{1,2}asdasdhbn{1,23}");
        }catch (ParsingException e) {
            System.out.println(e.getError().toString() + " / " + e.getData());
            throw e;
        }
    }


    @Test
    public void variationGenerator_test() {
        VariationsGenerator generator = new VariationsGenerator();
        generator.init("elo{test=1,2,3,4}xd{10-12}");
        assertEquals(2, generator.getCollection().size());
        assertEquals("test", generator.getCollection().get(0).getName());
        assertEquals(4, generator.getCollection().get(0).getValues().size());
        assertNotEquals(null, generator.getCollection().get(1).getName());
        assertEquals(3, generator.getCollection().get(1).getValues().size());
        assertEquals(12, generator.size());
    }
}
