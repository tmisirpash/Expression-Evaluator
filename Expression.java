package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
	public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	String [] temptokens = expr.split("\\s");
    	String shortened = "";
    	for (int y = 0; y < temptokens.length; y++)
    	{
    		shortened += temptokens[y];
    	}
    	String [] tokens = new String[shortened.length()];
    	for (int z = 0; z < shortened.length(); z++)
    	{
    		tokens[z] = shortened.substring(z,z+1);
    	}
    	String digits = "0123456789";
    	int i = 0;
    	for (i = 0; i < tokens.length; i++)
    	{
    		// If the given character is a letter
    		if (!delims.contains(tokens[i]) && !digits.contains(tokens[i]))
    		{
    			String variableName = "" + tokens[i];
    			boolean repeat = true;
    			while(repeat)
    			{
    				if (i == tokens.length - 1)
    				{
    					Variable var = new Variable(variableName);
    					if (!vars.contains(var))
    					{
    						System.out.println("Variable: " + variableName);
        					vars.add(var);
    					}
    					break;
    				}
    				else
    				{
    					i=i+1;
    				}
    				if (tokens[i].equals("["))
    				{
    					Array var = new Array(variableName);
    					if (!arrays.contains(var))
    					{
    						System.out.println("Array: " + variableName);
        					arrays.add(var);
    					}
    					repeat = false;
    				}
    				else if (!delims.contains(tokens[i]) && !digits.contains(tokens[i]))
    				{
    					variableName += tokens[i];
    				}
    				else
    				{
    					Variable var = new Variable(variableName);
    					if (!vars.contains(var))
    					{
    						System.out.println("Variable: " + variableName);
        					vars.add(var);
    					}
    					repeat = false;
    				}
    			}
    		}
    	}
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    {
    	String digits = "0123456789";
    	String [] temptokens = expr.split("\\s");
    	String shortened = "";
    	for (int y = 0; y < temptokens.length; y++)
    	{
    		shortened += temptokens[y];
    	} 
    	String word = "";
    	String simplified = "";
    	for (int i = 0; i < shortened.length(); i++)
    	{
    		
    		if (!delims.contains(shortened.substring(i,i+1))&& !digits.contains(shortened.substring(i,i+1)))
    		{
    			word += shortened.substring(i,i+1);
    		}
    		else if (delims.contains(shortened.substring(i,i+1)))
    		{
    			if (word.equals(""))
    			{
    				simplified += shortened.substring(i,i+1); 
    			}
    			else if (shortened.substring(i,i+1).equals("["))
    			{
    				simplified += word;
    				simplified += "[";
    				word = "";
    			}
    			else
    			{
    				for (int j = 0; j < vars.size(); j++)
    				{
    					if (vars.get(j).name.equals(word))
    					{
    						simplified += Integer.toString(vars.get(j).value);
    						simplified += shortened.substring(i,i+1);
    						word = "";
    					}
    				}
    			}
    		}
    		else if (digits.contains(shortened.substring(i,i+1)))
    		{
    			simplified += shortened.substring(i,i+1);
    		}
    		if (i == shortened.length()-1)
    		{
    			for (int j = 0; j < vars.size(); j++)
    			{
    				if (vars.get(j).name.equals(word))
    				{
    					simplified += Integer.toString(vars.get(j).value);
    					word = "";
    				}
    			}
    		}
    	}
    	int counter = countParenthesisPairs(simplified) + countBracketPairs(simplified);
    	for (int i = 0; i < counter; i++)
    	{
    		simplified = ParCalc3(simplified, arrays);
    	}
    	return simplify(simplified);
    }
    private static float simplify(String s)
	{
		String digits = "0123456789";
		String tempNumber = "";
		Stack<Float> numbers = new Stack<Float>();
		Stack<String> operations = new Stack<String>();
		for (int i = 0; i < s.length(); i++)
		{
			if (digits.contains(s.substring(i,i+1)) || s.substring(i,i+1).contentEquals("."))
			{
				tempNumber += s.substring(i,i+1);
			}
			else if (delims.contains(s.substring(i,i+1)))
			{
				if (i == 0 && s.substring(i,i+1).contentEquals("-"))
				{
					tempNumber = "-";
				}
				else if (s.substring(i,i+1).contentEquals("-") && (s.substring(i-1, i).contentEquals("-")))
				{
					operations.pop();
					operations.push("+");
				}
				else if (s.substring(i,i+1).contentEquals("-") && (s.substring(i-1, i).contentEquals("+")))
				{
					operations.pop();
					operations.push("-");
				}
				else if (s.substring(i,i+1).contentEquals("-") && (s.substring(i-1, i).contentEquals("*")))
				{
					tempNumber = "-";
				}
				else if (s.substring(i,i+1).contentEquals("-") && (s.substring(i-1, i).contentEquals("/")))
				{
					tempNumber = "-";
				}
				else
				{
					numbers.push(Float.parseFloat(tempNumber));
					operations.push(s.substring(i,i+1));
					tempNumber = "";
				}
			}
			else if (s.substring(i,i+1).equals("E"))
			{
				float coeff = Float.parseFloat(tempNumber);
				int z = i + 1;
				boolean moveOn = true;
				String newNumber = "";
				do
				{
					newNumber += s.substring(z,z+1);
					z++;
					if (z==s.length())
					{
						moveOn = false;
					}
				} while (moveOn && !delims.contains(s.substring(z, z+1)));
				float exponent = coeff*(float)Math.pow(10, Float.parseFloat(newNumber));
				tempNumber = "" + exponent;
				i = z-1;
			}
			if (i >= s.length()-1)
			{
				numbers.push(Float.parseFloat(tempNumber));
				tempNumber = "";
			}
		}
		float [] numArray = new float [numbers.size()];
		String [] opArray = new String[operations.size()];
		for (int i = numArray.length-1; i >= 0; i--)
		{
			numArray[i] = numbers.pop();
		}
		for (int i = opArray.length-1; i >= 0; i--)
		{
			opArray[i] = operations.pop();
		}
		int firstcheck = 0;
		for (int i = 0; i < opArray.length; i++)
		{
			if (opArray[i].equals("*") || opArray[i].contentEquals("/"))
			{
				firstcheck += 1;
			}
		}
		for (int i = 0; i < firstcheck; i++)
		{
			for (int j = 0; j < opArray.length; j++)
			{
				if (opArray[j].contentEquals("/"))
				{
					numArray[j] = numArray[j]/numArray[j+1];
					numArray = deleteIndexNums(j+1, numArray);
					opArray = deleteIndexOps(j, opArray);
					break;
				}
				else if (opArray[j].contentEquals("*"))
				{
					numArray[j] = numArray[j]*numArray[j+1];
					numArray = deleteIndexNums(j+1, numArray);
					opArray = deleteIndexOps(j, opArray);
					break;
				}
			}
		}
		int secondcheck = opArray.length;
		for (int i = 0; i < secondcheck; i++)
		{
			for (int j = 0; j < opArray.length; j++)
			{
				if (opArray[j].contentEquals("+"))
				{
					numArray[j] = numArray[j]+numArray[j+1];
					numArray = deleteIndexNums(j+1, numArray);
					opArray = deleteIndexOps(j, opArray);
					break;
				}
				else if (opArray[j].contentEquals("-"))
				{
					numArray[j] = numArray[j]-numArray[j+1];
					numArray = deleteIndexNums(j+1, numArray);
					opArray = deleteIndexOps(j, opArray);
					break;
				}
			}
		}
		return numArray[0];
	}
	private static String ParCalc3(String expr, ArrayList<Array> arrays)
	{
		String digits = "0123456789";
		if (countParenthesisPairs(expr) == 0 && countBracketPairs(expr) == 0)
		{
			return "" + simplify(expr);
		}
		else
		{
			int parbrackcounter = 0;
			int open = 0;
			int close = 0;
			for (int i = 0; i < expr.length(); i++)
			{
				if (expr.substring(i,i+1).contentEquals("(") || expr.substring(i,i+1).contentEquals("["))
				{
					parbrackcounter++;
					if (parbrackcounter == countParenthesisPairs(expr)+countBracketPairs(expr))
					{
						open = i;
						break;
					}
				}
			}
			for (int i = open + 1; i < expr.length(); i++)
			{
				if (expr.substring(i, i+1).contentEquals(")") || expr.substring(i,i+1).contentEquals("]"))
				{
						close = i;
						break;
					
				}
			}
			String expression = expr.substring(open + 1, close);
			String left = "";
			String right = "";
			if (expr.substring(open, open+1).contentEquals("["))
			{
				String arrName = "";
				for (int i = open - 1; i >= 0; i--)
				{
					if (!digits.contains(expr.substring(i,i+1)) && !delims.contains(expr.substring(i,i+1)))
					{
						arrName = expr.substring(i,i+1) + arrName;
					}
					else if (delims.contains(expr.substring(i,i+1)))
					{
						for (int z = 0; z < arrays.size(); z++)
						{
							if (arrays.get(z).name.contentEquals(arrName))
							{
								left = expr.substring(0, i+1);
								right = expr.substring(close+1, expr.length());
								return left + "" + arrays.get(z).values[(int)simplify(expression)] + right;
							}
						}
					}
					if (i == 0)
					{
						right = expr.substring(close + 1, expr.length());
						for (int z = 0; z < arrays.size(); z++)
						{
							if (arrays.get(z).name.contentEquals(arrName))
							{
								return arrays.get(z).values[(int)simplify(expression)] + right;
							}
						}
					}
				}
			}
			else
			{
			left = expr.substring(0, open);
			right = expr.substring(close + 1, expr.length());
			return left + "" + simplify(expression) + right;
			}
			
		}
		return "";
	}
    private static int countBracketPairs(String s)
    {
    	int counter = 0;
    	for (int i = 0; i < s.length(); i++)
    	{
    		if (s.substring(i,i+1).equals("["))
    		{
    			counter++;
    		}
    	}
    	return counter;
    }
    private static int countParenthesisPairs(String s)
    {
    	int counter = 0;
    	for (int i = 0; i < s.length(); i++)
    	{
    		if (s.substring(i,i+1).equals("("))
    		{
    			counter++;
    		}
    	}
    	return counter;
    }
	private static float [] deleteIndexNums(int index, float [] arr)
	{
		float [] updatedNums = new float [arr.length-1];
		for (int i = 0, j = 0; i < arr.length; i++)
		{
			if (i == index)
			{
				continue;
			}
			updatedNums[j++] = arr[i];
		}
		return updatedNums;
	}
	private static String [] deleteIndexOps(int index, String [] arr)
	{
		String [] updatedOps = new String [arr.length-1];
		for (int i = 0, j = 0; i < arr.length; i++)
		{
			if (i == index)
			{
				continue;
			}
			updatedOps[j++] = arr[i];
		}
		return updatedOps;
	}
}
