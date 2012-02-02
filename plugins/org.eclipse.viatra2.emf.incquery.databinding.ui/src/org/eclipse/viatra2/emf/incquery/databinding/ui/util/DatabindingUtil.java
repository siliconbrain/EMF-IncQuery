package org.eclipse.viatra2.emf.incquery.databinding.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.viatra2.emf.incquery.databinding.runtime.DatabindingAdapter;
import org.eclipse.viatra2.emf.incquery.databinding.ui.observable.PatternMatcherRoot;
import org.eclipse.viatra2.emf.incquery.databinding.ui.observable.RuntimeDatabindingAdapter;
import org.eclipse.viatra2.emf.incquery.databinding.ui.observable.ViewerRootKey;
import org.eclipse.viatra2.emf.incquery.runtime.api.IMatcherFactory;
import org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature;
import org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.viatra2.patternlanguage.EMFPatternLanguageStandaloneSetup;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Annotation;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.AnnotationParameter;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Pattern;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.ValueReference;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.impl.StringValueImpl;
import org.eclipse.viatra2.patternlanguage.eMFPatternLanguage.PatternModel;
import org.eclipse.viatra2.patternlanguage.emf.matcherbuilder.runtime.PatternRegistry;

import com.google.inject.Injector;

/**
 * The util contains several useful methods for the databinding operations.
 * 
 * @author Tamas Szabo
 *
 */
public class DatabindingUtil {

	public static Map<IFile, PatternModel> registeredPatterModels = new HashMap<IFile, PatternModel>();
	
	/**
	 * Get the PatternUI annotation's message attribute for the pattern whose name is patternName. 
	 * 
	 * @param patternName the name of the pattern
	 * @return the content of the message attribute
	 */
	public static String getMessage(String patternName, boolean generatedMatcher) {
		
		if (generatedMatcher) {
			try {
				IExtensionRegistry reg = Platform.getExtensionRegistry();
				IExtensionPoint ep = reg
						.getExtensionPoint("org.eclipse.viatra2.emf.incquery.databinding.runtime.databinding");
				for (IExtension e : ep.getExtensions()) {
					for (IConfigurationElement ce : e.getConfigurationElements()) {
						String[] tokens = patternName.split("\\.");
						String pattern = tokens[tokens.length - 1];
	
						if (ce.getName().equals("databinding")
								&& ce.getAttribute("patternName").equalsIgnoreCase(
										pattern)) {
							return ce.getAttribute("message");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			for (IFile key : registeredPatterModels.keySet()) {
				for (Pattern p : registeredPatterModels.get(key).getPatterns()) {
					if (PatternRegistry.fqnOf(p).matches(patternName)) {
						for (Annotation a : p.getAnnotations()) {
							if (a.getName().matches("PatternUI")) {
								for (AnnotationParameter ap : a.getParameters()) {
									if (ap.getName().matches("message")) {
										ValueReference valRef = ap.getValue();
										if (valRef instanceof StringValueImpl) {
											return ((StringValueImpl) valRef).getValue();
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Get the DatabindingAdapter generated for the pattern whose name is patternName
	 * 
	 * @param patternName the name of the pattern
	 * @return an instance of the DatabindingAdapter class generated for the pattern
	 */
	@SuppressWarnings("unchecked")
	public static DatabindingAdapter<IPatternSignature> getDatabindingAdapter(String patternName, boolean generatedMatcher) {
		if (generatedMatcher) {
			try {
				IExtensionRegistry reg = Platform.getExtensionRegistry();
				IExtensionPoint ep = reg.getExtensionPoint("org.eclipse.viatra2.emf.incquery.databinding.runtime.databinding");
				for (IExtension e : ep.getExtensions()) {
					for (IConfigurationElement ce : e.getConfigurationElements()) {
						String[] tokens = patternName.split("\\.");
						String pattern = tokens[tokens.length - 1];
						
						if (ce.getName().equals("databinding") && ce.getAttribute("patternName").equalsIgnoreCase(pattern)) {
							Object obj = ce.createExecutableExtension("class");
	
							if (obj != null && obj instanceof DatabindingAdapter) {
								return (DatabindingAdapter<IPatternSignature>) obj;
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Could not find DatabindableMatcher for pattern named: "+patternName);
			}
		}
		else {
			RuntimeDatabindingAdapter adapter = new RuntimeDatabindingAdapter();
			
			for (IFile file : registeredPatterModels.keySet()) {
				for (Pattern p : registeredPatterModels.get(file).getPatterns()) {
					if (PatternRegistry.fqnOf(p).matches(patternName)) {
						for (Annotation a : p.getAnnotations()) {
							if (a.getName().matches("ObservableValue")) {
								String key = null, value = null;
								
								for (AnnotationParameter ap : a.getParameters()) {
									if (ap.getName().matches("name")) {
										ValueReference valRef = ap.getValue();
										if (valRef instanceof StringValueImpl) {
											key = ((StringValueImpl) valRef).getValue();
										}
									}
									
									if (ap.getName().matches("expression")) {
										ValueReference valRef = ap.getValue();
										if (valRef instanceof StringValueImpl) {
											value = ((StringValueImpl) valRef).getValue();
										}
									}
								}
								
								if (key != null && value != null) {
									adapter.putToParameterMap(key, value);
								}
							}
						}
					}
				}
			}
			
			return adapter;
		}

		return null;
	}

	/**
	 * Get the structural feature with the given name of the given object.
	 * 
	 * @param o the object (must be an EObject)
	 * @param featureName the name of the feature
	 * @return the EStructuralFeature of the object or null if it can not be found
	 */
	public static EStructuralFeature getFeature(Object o, String featureName) {
		if (o != null && o instanceof EObject) {
			EStructuralFeature feature = ((EObject) o).eClass()
					.getEStructuralFeature(featureName);
			return feature;
		}
		return null;
	}

	/**
	 * Registers the given changeListener for the appropriate features of the given signature.
	 * The features will be computed based on the message parameter.
	 * 
	 * @param signature the signature instance
	 * @param changeListener the changle listener 
	 * @param message the message which can be found in the appropriate PatternUI annotation
	 */
	public static void observeFeatures(IPatternSignature signature,	IValueChangeListener changeListener, String message) {
		if (message != null) {
			String[] tokens = message.split("\\$");

			for (int i = 0; i < tokens.length; i++) {
				if (i % 2 != 0) {
					String[] objectTokens = tokens[i].split("\\.");
					if (objectTokens.length == 2) {
						Object o = signature.get(objectTokens[0]);
						EStructuralFeature feature = getFeature(o, objectTokens[1]);
						if (o != null && feature != null) {
							IObservableValue val = EMFProperties.value(feature).observe(o);
							val.addValueChangeListener(changeListener);
						}
					}
				}
			}
		}
	}

	/**
	 * Create a PatternMatcher root for the given key element.
	 * 
	 * @param key the key element (editorpart + resource set)
	 * @return the PatternMatcherRoot element
	 */
	@SuppressWarnings({ "unchecked" })
	public static PatternMatcherRoot createPatternMatcherRoot(ViewerRootKey key) {
		PatternMatcherRoot result = new PatternMatcherRoot(key);

		//generated matchers
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint("org.eclipse.viatra2.emf.incquery.databinding.runtime.databinding");
		for (IExtension e : ep.getExtensions()) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				try {
					Object obj = ce.createExecutableExtension("matcherFactoryClass");

					if (obj instanceof IMatcherFactory<?, ?>) {
						IMatcherFactory<IPatternSignature, IncQueryMatcher<IPatternSignature>> factory = (IMatcherFactory<IPatternSignature, IncQueryMatcher<IPatternSignature>>) obj;
						IncQueryMatcher<IPatternSignature> matcher = factory.getMatcher(key.getNotifier());

						result.addMatcher(matcher, true);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//runtime matchers
		for (IFile file : registeredPatterModels.keySet()) {
			result.registerPatternsFromFile(file, registeredPatterModels.get(file));
		}

		return result;
	}
	
	public static PatternModel parseEPM(IFile file) {
		Injector injector = new EMFPatternLanguageStandaloneSetup().createInjectorAndDoEMFRegistration();
		if (file == null) {
			return null;
		}

		ResourceSet resourceSet = injector.getInstance(ResourceSet.class);
		URI fileURI = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
		Resource resource = resourceSet.getResource(fileURI, true);
		if (resource != null && resource.getContents().size() >= 1) {
			EObject topElement = resource.getContents().get(0);
			return topElement instanceof PatternModel ? (PatternModel) topElement : null;
		} 
		else {
			return null;
		}
	}
}
