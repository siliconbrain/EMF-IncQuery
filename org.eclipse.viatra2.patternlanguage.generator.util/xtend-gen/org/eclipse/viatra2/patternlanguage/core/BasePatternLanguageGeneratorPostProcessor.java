package org.eclipse.viatra2.patternlanguage.core;

import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.common.util.BasicEMap.Entry;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.GeneratedMetamodel;
import org.eclipse.xtext.xbase.lib.CollectionExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IntegerExtensions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xtext.ecoreInference.IXtext2EcorePostProcessor;

@SuppressWarnings("all")
public class BasePatternLanguageGeneratorPostProcessor implements IXtext2EcorePostProcessor {
  public void process(final GeneratedMetamodel metamodel) {
    EPackage _ePackage = metamodel.getEPackage();
    this.process(_ePackage);
  }
  
  public void process(final EPackage p) {
      EClass patternClass = null;
      EClass bodyClass = null;
      EClass varClass = null;
      EClass varRefClass = null;
      EClass pathExpressionConstraint = null;
      EClass pathExpressionElement = null;
      EClass pathExpressionHead = null;
      EClass pathExpressionTail = null;
      EList<EClassifier> _eClassifiers = p.getEClassifiers();
      Iterable<EClass> _filter = IterableExtensions.<EClass>filter(_eClassifiers, org.eclipse.emf.ecore.EClass.class);
      for (final EClass c : _filter) {
        String _name = c.getName();
        final String __valOfSwitchOver = _name;
        boolean matched = false;
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"Pattern")) {
            matched=true;
            patternClass = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"PatternBody")) {
            matched=true;
            bodyClass = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"Variable")) {
            matched=true;
            varClass = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"VariableReference")) {
            matched=true;
            varRefClass = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"PathExpressionConstraint")) {
            matched=true;
            pathExpressionConstraint = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"PathExpressionElement")) {
            matched=true;
            pathExpressionElement = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"PathExpressionHead")) {
            matched=true;
            pathExpressionHead = c;
          }
        }
        if (!matched) {
          if (ObjectExtensions.operator_equals(__valOfSwitchOver,"PathExpressionTail")) {
            matched=true;
            pathExpressionTail = c;
          }
        }
      }
      this.generateEReference(bodyClass, varClass);
      this.generateReferenceToVariableDecl(varClass, varRefClass);
      this.generateEOperation(bodyClass, varClass);
      this.changeHeadType(pathExpressionConstraint, pathExpressionHead);
      this.changeTailType(pathExpressionElement, pathExpressionTail);
  }
  
  public void generateInverseContainerOfBody(final EClass bodyClass, final EClass patternClass) {
      EReference _createEReference = EcoreFactory.eINSTANCE.createEReference();
      final EReference patternRef = _createEReference;
      patternRef.setTransient(true);
      patternRef.setDerived(true);
      patternRef.setName("pattern");
      patternRef.setLowerBound(1);
      patternRef.setUpperBound(1);
      patternRef.setChangeable(true);
      patternRef.setContainment(true);
      EStructuralFeature _eStructuralFeature = patternClass.getEStructuralFeature("bodies");
      patternRef.setEOpposite(((EReference) _eStructuralFeature));
  }
  
  public boolean generateEReference(final EClass bodyClass, final EClass varClass) {
    boolean _xblockexpression = false;
    {
      EReference _createEReference = EcoreFactory.eINSTANCE.createEReference();
      final EReference varRef = _createEReference;
      varRef.setTransient(true);
      varRef.setDerived(true);
      varRef.setName("variables");
      varRef.setLowerBound(0);
      int _operator_minus = IntegerExtensions.operator_minus(1);
      varRef.setUpperBound(_operator_minus);
      varRef.setEType(varClass);
      varRef.setChangeable(false);
      varRef.setContainment(true);
      EAnnotation _createEAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
      final EAnnotation body = _createEAnnotation;
      body.setSource(GenModelPackage.eNS_URI);
      EClass _eStringToStringMapEntry = EcorePackage.eINSTANCE.getEStringToStringMapEntry();
      EObject _create = EcoreFactory.eINSTANCE.create(_eStringToStringMapEntry);
      final Entry<String,String> map = ((Entry<String,String>) _create);
      map.setKey("suppressedGetVisibility");
      map.setValue("true");
      EMap<String,String> _details = body.getDetails();
      _details.add(map);
      EList<EAnnotation> _eAnnotations = varRef.getEAnnotations();
      CollectionExtensions.<EAnnotation>operator_add(_eAnnotations, body);
      EList<EStructuralFeature> _eStructuralFeatures = bodyClass.getEStructuralFeatures();
      boolean _operator_add = CollectionExtensions.<EReference>operator_add(_eStructuralFeatures, varRef);
      _xblockexpression = (_operator_add);
    }
    return _xblockexpression;
  }
  
  /**
   * Genearates a variable reference (and its opposite) in the pattern body and its usages.
   */
  public void generateReferenceToVariableDecl(final EClass varClass, final EClass varRefClass) {
      EReference _createEReference = EcoreFactory.eINSTANCE.createEReference();
      final EReference varRefs = _createEReference;
      varRefs.setTransient(true);
      varRefs.setDerived(true);
      varRefs.setName("references");
      varRefs.setLowerBound(0);
      int _operator_minus = IntegerExtensions.operator_minus(1);
      varRefs.setUpperBound(_operator_minus);
      varRefs.setEType(varRefClass);
      varRefs.setContainment(false);
      EList<EStructuralFeature> _eStructuralFeatures = varClass.getEStructuralFeatures();
      CollectionExtensions.<EReference>operator_add(_eStructuralFeatures, varRefs);
      EReference _createEReference_1 = EcoreFactory.eINSTANCE.createEReference();
      final EReference variable = _createEReference_1;
      variable.setTransient(true);
      variable.setDerived(true);
      variable.setName("variable");
      variable.setLowerBound(0);
      variable.setUpperBound(1);
      variable.setEType(varClass);
      variable.setContainment(false);
      EList<EStructuralFeature> _eStructuralFeatures_1 = varRefClass.getEStructuralFeatures();
      CollectionExtensions.<EReference>operator_add(_eStructuralFeatures_1, variable);
      varRefs.setEOpposite(variable);
      variable.setEOpposite(varRefs);
  }
  
  /**
   * Generates an EOperation that corresponds with the derived attribute called ''variables''
   * of the PatternBody.
   */
  public boolean generateEOperation(final EClass bodyClass, final EClass varClass) {
    boolean _xblockexpression = false;
    {
      EOperation _createEOperation = EcoreFactory.eINSTANCE.createEOperation();
      final EOperation op = _createEOperation;
      op.setName("getVariables");
      op.setEType(varClass);
      int _operator_minus = IntegerExtensions.operator_minus(1);
      op.setUpperBound(_operator_minus);
      EAnnotation _createEAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
      final EAnnotation body = _createEAnnotation;
      body.setSource(GenModelPackage.eNS_URI);
      EClass _eStringToStringMapEntry = EcorePackage.eINSTANCE.getEStringToStringMapEntry();
      EObject _create = EcoreFactory.eINSTANCE.create(_eStringToStringMapEntry);
      final Entry<String,String> map = ((Entry<String,String>) _create);
      map.setKey("body");
      map.setValue("\tif (variables == null)\n\t{\n\t    variables = new EObjectContainmentEList<Variable>(Variable.class, this, PatternLanguagePackage.PATTERN_BODY__VARIABLES);\n\t}  \n    EList<Variable> parameters = ((org.eclipse.viatra2.patternlanguage.core.patternLanguage.Pattern)eContainer).getParameters();\n    java.util.Iterator<org.eclipse.emf.ecore.EObject> it = eAllContents();\n    java.util.HashMap<String, org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference> variables = new java.util.HashMap<String, org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference>();\n    while(it.hasNext()) {\n       org.eclipse.emf.ecore.EObject obj = it.next(); \n       if (obj instanceof org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference \n         && !variables.containsKey(((org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference) obj).getVar())) {\n         variables.put(((org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference)obj).getVar(), (org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference) obj);\n      }\n    }\n    java.util.Hashtable<String, Variable> varDefs = new java.util.Hashtable<String, Variable>();\n    for (Variable var : parameters) {\n      varDefs.put(var.getName(), var);\n    }\n    if (this.variables != null) {\n\t    for (Variable var : this.variables) {\n\t       \tif (variables.containsKey(var.getName())) {\n                varDefs.put(var.getName(), var);\n\t       \t}\n\t    }\n    }\n    for (String name : variables.keySet()) {\n       if (!varDefs.containsKey(name)) {\n       \t  Variable decl = org.eclipse.viatra2.patternlanguage.core.patternLanguage.PatternLanguageFactory.eINSTANCE.createVariable();\n       \t  decl.setName(name);\n       \t  this.variables.add(decl);\n          varDefs.put(name, decl);\n       }\n    }\n\tit = eAllContents();\n    while(it.hasNext()) {\n      org.eclipse.emf.ecore.EObject obj = it.next(); \n      if (obj instanceof org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference) {\n      org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference ref = (org.eclipse.viatra2.patternlanguage.core.patternLanguage.VariableReference)obj;\n      String name = ref.getVar();\n      Variable var = varDefs.get(name);\n      ref.setVariable(var);\n\n      }\n    }\n\treturn this.variables;");
      EMap<String,String> _details = body.getDetails();
      _details.add(map);
      EList<EAnnotation> _eAnnotations = op.getEAnnotations();
      CollectionExtensions.<EAnnotation>operator_add(_eAnnotations, body);
      EList<EOperation> _eOperations = bodyClass.getEOperations();
      boolean _operator_add = CollectionExtensions.<EOperation>operator_add(_eOperations, op);
      _xblockexpression = (_operator_add);
    }
    return _xblockexpression;
  }
  
  public void changeHeadType(final EClass constraint, final EClass head) {
    EList<EStructuralFeature> _eStructuralFeatures = constraint.getEStructuralFeatures();
    final Function1<EStructuralFeature,Boolean> _function = new Function1<EStructuralFeature,Boolean>() {
        public Boolean apply(final EStructuralFeature e) {
          String _name = e.getName();
          boolean _operator_equals = ObjectExtensions.operator_equals(_name, "head");
          return Boolean.valueOf(_operator_equals);
        }
      };
    EStructuralFeature _findFirst = IterableExtensions.<EStructuralFeature>findFirst(_eStructuralFeatures, _function);
    _findFirst.setEType(head);
  }
  
  /**
   * The method updates the EClass element: it changes the type of the "tail" EStructuralFeature to the second parameter
   * @param element the EClass to change
   * @param tail the type to set
   */
  public void changeTailType(final EClass element, final EClass tail) {
    EList<EStructuralFeature> _eStructuralFeatures = element.getEStructuralFeatures();
    final Function1<EStructuralFeature,Boolean> _function = new Function1<EStructuralFeature,Boolean>() {
        public Boolean apply(final EStructuralFeature e) {
          String _name = e.getName();
          boolean _operator_equals = ObjectExtensions.operator_equals(_name, "tail");
          return Boolean.valueOf(_operator_equals);
        }
      };
    EStructuralFeature _findFirst = IterableExtensions.<EStructuralFeature>findFirst(_eStructuralFeatures, _function);
    _findFirst.setEType(tail);
  }
}
