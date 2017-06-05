package com.twino.ls.base.util;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class CachedExpressions {

	private SpelExpressionParser expressionParser = new SpelExpressionParser();
	private Map<String, Expression> cachedExpressions = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T getValue(Object source, String expressionString) {
		Expression expression = parse(expressionString);
		return (T) expression.getValue(source);
	}

	public void setValue(Object target, String expressionString, Object newValue) {
		checkNotNull(target, "Target undefined");
		Expression expression = parse(expressionString);
		expression.setValue(target, newValue);
	}

	private Expression parse(String expressionString) {
		String nullSafeExpression = expressionString.replace(".", "?.");
		Expression expression = cachedExpressions.get(nullSafeExpression);
		if (expression == null) {
			expression = expressionParser.parseExpression(nullSafeExpression);
			cachedExpressions.put(nullSafeExpression, expression);
		}
		return expression;
	}

}
