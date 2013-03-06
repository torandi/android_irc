package com.torandi.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.torandi.lib.GetOpt.ArgumentException.type;

public class GetOpt {
	 /**
	  *  Fail if an malformed argument is found (exception)
	  *  If this is not set the getopt is instead terminated on malformed argument (return -1)
	  */
	public static final int FAIL_ON_MALFORMED = 1;

	public GetOpt(Option[] options) {
		this(options, 0);
	}
	
	public GetOpt(Option[] options, int flags) {
		this.options = options;
	
		if((flags & FAIL_ON_MALFORMED) == 1) {
			fail_on_malformed = true;
		}
		
		short_match = new Option[256];
		for(Option opt : options) {
			short_match[opt.shortopt] = opt;
		}
	}
	
	/**
	 * Parses next option
	 * @param parse_pair Parse pair, contains state. Create initial with ParsePair(args)
	 * @param argument Filled with the argument for the option or null
	 * @return the short char for the option, or -1 if end of list
	 * 		
	 */
	public char parse(ParsePair parse_pair, StringWrapper argument) throws ArgumentException{
		String next = parse_pair.next();
		if(next == null) return (char)-1;
		
		Matcher m = short_pattern.matcher(next);
		
		if(next.matches("^--.+$")) { //long options:
			for(Option opt : options) {
				if(opt.match(parse_pair, argument)) {
					return opt.shortopt;
				}
			}
			throw new ArgumentException("Unknown argument "+next, type.INVALID_OPTION);
		} else if(m.matches()){
			/*
			 * Short option matching
			 * -a -b -c option
			 * -abc option
			 * -ab -coption
			 * -abcoption
			 */
			String sopts = m.group(1);
			if(parse_pair.internal_index<sopts.length()) {
				int index = parse_pair.internal_index++;
				char s = sopts.charAt(index);
				Option opt = short_match[s];
				if(opt != null) {
					switch(opt.has_arg) {
					case NO_ARGUMENT:
						argument = null;
						break;
					case OPTIONAL_ARGUMENT:
						//Test all non-argument cases, and if so, set argument to null and break
						if(! (
								(
								parse_pair.internal_index != sopts.length() 
								&& short_match[parse_pair.internal_index] != null
								)
							|| 
								parse_pair.has_next()
							)
								) {
							argument.str = null;
							break;
						}
						//So there is a argument, fall through and parse it
					case REQUIRED_ARGUMENT:
						if(parse_pair.internal_index != sopts.length()) {
							//All remaining chars of the group is the option
							argument.str = sopts.substring(parse_pair.internal_index);
							parse_pair.step();
						} else if(parse_pair.has_next()) {
							parse_pair.step();
							argument.str = parse_pair.next();
							parse_pair.step();
						} else {
							throw new ArgumentException("Option -"+s+" requires an argument, non given.", type.MISSING_ARGUMENT);
						}
						break;
					}
					
					return opt.shortopt;
				} else {
					parse_pair.step();
					throw new ArgumentException("Unknown option -"+s, type.INVALID_OPTION);
				}
			} else {
				parse_pair.step();
				return parse(parse_pair, argument);
			}
			
		} else {
			if(fail_on_malformed) {
				throw new ArgumentException("Malformed argument "+next, type.MALFORMED_ARGUMENT);
			} else{
				return (char)-1;
			}
		}
	}
	
	public void print_usage() {
		for(Option opt : options) {
			opt.print_descr();
		}
	}
	
	private Option[] options;
	private Option[] short_match;
	private boolean fail_on_malformed = false;
	private final static Pattern short_pattern = Pattern.compile("^-([^-]+)$");
	
	public static class ParsePair {
		/**
		 * I hate java for not having pointers, real references and functions as arguments
		 */
		String[] args;
		int index;
		int internal_index;
		
		public ParsePair(String[] args) {
			this.args = args;
			index = 0;
			internal_index = 0;
		}
		
		public String next() {
			if(index < args.length) {
				return args[index];
			} else {
				return null;
			}
		}
		
		public void step() {
			++index;
			internal_index = 0;
		}
		
		public boolean has_next() {
			return (index + 1) < args.length;
		}
		
		public boolean has_current() {
			return index < args.length;
		}
	}
	
	public static class Option {
		
		
		public enum arg_style {
			NO_ARGUMENT,
			REQUIRED_ARGUMENT,
			OPTIONAL_ARGUMENT
		}
		
		/**
		 * Create a option
		 * @param longopt The longopt (--longopt)
		 * @param shortopt The shortopt (-s) (must be unique)
		 * @param has_arg Indicate if the option requires an argument
		 * @param description description for usage print
		 */
		public Option(String longopt, char shortopt, arg_style has_arg, String description) {
			this.longopt = longopt;
			this.shortopt = shortopt;
			this.has_arg = has_arg;
			this.description = description;
			String long_base = "^--"+longopt;
			switch(has_arg) {
			case NO_ARGUMENT:
				long_pattern = Pattern.compile(long_base + "$", Pattern.CASE_INSENSITIVE);
				break;
			case REQUIRED_ARGUMENT:
				long_pattern = Pattern.compile(long_base + "=(.+)$", Pattern.CASE_INSENSITIVE);
				break;
			case OPTIONAL_ARGUMENT:
				long_pattern = Pattern.compile(long_base + "(?:=(.+))?$", Pattern.CASE_INSENSITIVE);
				break;
			}
			
		}
		
		/**
		 * Same, but without description
		 */
		public Option(String longopt, char shortopt, arg_style has_arg) {
			this(longopt, shortopt, has_arg, "");
		}
		
		/**
		 * @param parse_pair
		 * @param ret_argument Filled with argument if match
		 * @return match true if matched
		 */
		public boolean match(ParsePair parse_pair, StringWrapper ret_argument) throws ArgumentException {
			Matcher m;
			m = long_pattern.matcher(parse_pair.next());
			if(m.matches()) {
				parse_pair.step();
				if(m.groupCount() == 1) {
					ret_argument.str = m.group(1);
					if(ret_argument.str.isEmpty() && has_arg == arg_style.REQUIRED_ARGUMENT) {
						throw new ArgumentException("Missing argument for option --"+longopt, type.MISSING_ARGUMENT);
					} else if(ret_argument.str.isEmpty()){
						ret_argument.str = null;
					}
				} else {
					ret_argument.str = null;
				}
				return true;
			}
			
			if(parse_pair.next().equals("--"+longopt) && has_arg == arg_style.REQUIRED_ARGUMENT) {
				parse_pair.step();
				throw new ArgumentException("Missing required argument for option --"+longopt, type.MISSING_ARGUMENT);
			}
			return false;
		}
		
		public void print_descr() {
			String argument = "";
			switch(has_arg) {
			case NO_ARGUMENT:
				break;
			case REQUIRED_ARGUMENT:
				argument="=VALUE";
				break;
			case OPTIONAL_ARGUMENT:
				argument="[=VALUE]";
				break;
			}
			System.out.printf("\t-%c, --%s%s\t%s\n", shortopt, longopt, argument,description);
		}
		
		private String longopt;
		private char shortopt;
		private arg_style has_arg;
		private String description;
		private Pattern long_pattern;
 	}
	
	@SuppressWarnings("serial")
	public static class ArgumentException extends java.lang.Exception {
		public enum type {
			MISSING_ARGUMENT, /* option with required_argument lacks argument */
			INVALID_OPTION, /* unknown option present */
			MALFORMED_ARGUMENT
		}
		
		public final type err_type;
		
		public ArgumentException(String message, type err_type) {
			super(message);
			this.err_type = err_type;
		}
	}
}
