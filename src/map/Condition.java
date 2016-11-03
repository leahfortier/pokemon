package map;

import main.Game;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Condition {
	private static final Pattern conditionPattern = Pattern.compile("condition:\\s*(\\S+)");
	private static final Pattern functionPattern = Pattern.compile("(\\w+)|([()&|!])");
	
	/*
	 * postfixed boolean function
	 */
	private List<String> condition;
	private String originalConditionString;
	
	public Condition(String str) {
		if (StringUtils.isNullOrEmpty(str)) {
			str = StringUtils.empty();
		}

		originalConditionString = StringUtils.empty();
		condition = new ArrayList<>();
		Matcher conditionMatcher = conditionPattern.matcher(str);
		
		if (conditionMatcher.find()) {
			String function = conditionMatcher.group(1);
			Matcher m = functionPattern.matcher(function);
			Stack<String> stack = new Stack<>();
			
			while (m.find()) {
				String s = m.group();
				originalConditionString += s;

				switch (s) {
					case "(":
						stack.push(s);
						break;
					case ")":
						while (!stack.peek().equals("("))
							condition.add(stack.pop());

						stack.pop();
						break;
					case "&":
						while (!stack.isEmpty() && stack.peek().equals("!"))
							condition.add(stack.pop());

						stack.push(s);
						break;
					case "|":
						while (!stack.isEmpty() && (stack.peek().equals("&") || stack.peek().equals("!")))
							condition.add(stack.pop());

						stack.push(s);
						break;
					case "!":
						stack.push(s);
						break;
					default:
						condition.add(s);
						break;
				}
			}
			
			while (!stack.isEmpty()) {
				condition.add(stack.pop());
			}
		}
		else {
			condition.add("true");
		}
	}
	
	public boolean isTrue() {
		Stack<Boolean> stack = new Stack<>();
		for (String s: condition) {
			switch (s) {
				case "&": {
					boolean v1 = stack.pop();
					boolean v2 = stack.pop();

					stack.push(v1 && v2);
					break;
				}
				case "|": {
					boolean v1 = stack.pop();
					boolean v2 = stack.pop();

					stack.push(v1 || v2);
					break;
				}
				case "!":
					stack.push(!stack.pop());
					break;
				case "true":
					stack.push(true);
					break;
				case "false":
					stack.push(false);
					break;
				default:
					stack.push(Game.getPlayer().globalsContain(s));
					break;
			}
		}
		
		return stack.pop();
	}
	
	public boolean add(String global, char op) {
		if (op != '&' && op != '|') {
			return false;
		}
		
		originalConditionString = (originalConditionString.isEmpty()
				? StringUtils.empty()
				: ("(" + originalConditionString + ")" + op));
		originalConditionString += global;
		
		boolean negate = false;
		if (global.charAt(0) == '!') {
			global = global.substring(1, global.length());
			negate = true;
		}
		
		boolean noOp = false;
		if (condition.size() == 1 && condition.get(0).equals("true")) {
			condition.clear();
			noOp = true;
		}
		
		condition.add(global);
		if (negate) {
			condition.add("!");
		}
		
		if (!noOp) {
			condition.add(op + "");
		}
		
		return true;
	}
	
	public String toString() {
		return condition.toString();
	}
	
	public String getOriginalConditionString() {
		return originalConditionString;
	}
}
