package tsandmeier.ba.normalization;

import java.util.regex.Matcher;

public abstract class AbstractInterpreterBuilder {

	public abstract AbstractInterpreterBuilder interprete(final String surfaceForm);

	public abstract AbstractInterpreterBuilder fromMatcher(final Matcher matcher);

	public abstract IDatatypeInterpretation build();

}
