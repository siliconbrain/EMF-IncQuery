package org.eclipse.viatra2.patternlanguage.validation;

/**
 * @author Mark Czotter
 */
public final class EMFIssueCodes {

	protected static final String ISSUE_CODE_PREFIX = "org.eclipse.viatra2.patternlanguage.validation.IssueCodes.";
	
	public static final String DUPLICATE_IMPORT = ISSUE_CODE_PREFIX + "duplicate_import";
	public static final String INVALID_ENUM_LITERAL = ISSUE_CODE_PREFIX + "invalid_enum";
	
	public static final String SYMBOLIC_VARIABLE_NEVER_REFERENCED = ISSUE_CODE_PREFIX + "symbolic_variable_never_referenced";
	public static final String SYMBOLIC_VARIABLE_NO_POSITIVE_REFERENCE = ISSUE_CODE_PREFIX + "symbolic_variable_no_positive_reference";
	public static final String LOCAL_VARIABLE_REFERENCED_ONCE = ISSUE_CODE_PREFIX + "local_variable_referenced_once";
	public static final String LOCAL_VARIABLE_NO_QUANTIFYING_REFERENCE = ISSUE_CODE_PREFIX + "local_variable_no_quantifying_reference";
	public static final String LOCAL_VARIABLE_NO_POSITIVE_REFERENCE = ISSUE_CODE_PREFIX + "local_variable_no_positive_reference";

}
