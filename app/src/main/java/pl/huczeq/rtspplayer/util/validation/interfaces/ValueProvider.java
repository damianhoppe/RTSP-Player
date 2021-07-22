package pl.huczeq.rtspplayer.util.validation.interfaces;

public interface ValueProvider<T> {

    T provideValue();

    static <T> ValueProvider<T> transform(ValueTransform<T> transform, ValueProvider<T> valueProvider) {
        return new ValueProvider<T>() {
            @Override
            public T provideValue() {
                return transform.transform(valueProvider.provideValue());
            }
        };
    }

    static <T> FieldRule fieldRuleWith(ValueTransform<T> transform, FieldRule fieldRule) {
        return new FieldRule() {
            @Override
            public Integer checkValidity(String text) {
                return null;
            }
        };
    }
}
