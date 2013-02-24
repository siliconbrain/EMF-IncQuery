/*******************************************************************************
 * Copyright (c) 2010-2013, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.viewers.runtime.model.converters;

import java.util.Map;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.incquery.patternlanguage.helper.CorePatternLanguageHelper;
import org.eclipse.incquery.patternlanguage.patternLanguage.Annotation;
import org.eclipse.incquery.patternlanguage.patternLanguage.StringValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.VariableValue;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.viewers.runtime.model.Edge;
import org.eclipse.incquery.viewers.runtime.model.FormatSpecification;
import org.eclipse.incquery.viewers.runtime.model.Item;

import com.google.common.base.Preconditions;

/**
 * A converter from {@link IPatternMatch} matches to displayable {@link Edge} objects.
 * 
 * @author Zoltan Ujhelyi
 * 
 */
public class EdgeConverter implements IConverter {
    private String labelParameterName;
    private String sourceParameterName;
    private String destParameterName;
    private Map<Object, Item> itemMap;
    private FormatSpecification format;

    public EdgeConverter(Annotation itemAnnotation, Annotation formatAnnotation, Map<Object, Item> itemMap) {
        Preconditions.checkArgument(Edge.ANNOTATION_ID.equals(itemAnnotation.getName()),
                "The converter should be initialized using a " + Edge.ANNOTATION_ID + " annotation.");
        this.itemMap = itemMap;

        sourceParameterName = ((VariableValue) CorePatternLanguageHelper.getFirstAnnotationParameter(itemAnnotation,
                "source")).getValue().getVar();
        destParameterName = ((VariableValue) CorePatternLanguageHelper.getFirstAnnotationParameter(itemAnnotation,
                "target")).getValue().getVar();
        StringValue labelParam = (StringValue) CorePatternLanguageHelper.getFirstAnnotationParameter(itemAnnotation,
                "label");
        labelParameterName = labelParam == null ? "" : labelParam.getValue();

        if (formatAnnotation != null) {
            format = FormatParser.parseFormatAnnotation(formatAnnotation);
        }
    }

    @Override
    public Object getToType() {
        return Edge.class;
    }

    @Override
    public Object getFromType() {
        return IPatternMatch.class;
    }

    @Override
    public Object convert(Object fromObject) {
        IPatternMatch match = (IPatternMatch) fromObject;

        EObject sourceValue = (EObject) match.get(sourceParameterName);
        EObject destValue = (EObject) match.get(destParameterName);

        Item sourceItem = itemMap.get(sourceValue);
        Item destItem = itemMap.get(destValue);

        Edge edge = new Edge(sourceItem, destItem, match, labelParameterName);
        edge.setSpecification(format);
        return edge;
    }
}