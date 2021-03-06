package org.apache.flex.compiler.internal.css;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSProperty;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.css.ICSSSelector;
import org.apache.flex.compiler.internal.tree.mxml.MXMLNodeBaseTests;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleNode;
import org.junit.Ignore;

import com.google.common.collect.ImmutableList;

@Ignore
public class CSSBaseTests extends MXMLNodeBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	private String getPrefix()
	{
		return "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*' xmlns:s='library://ns.adobe.com/flex/spark' xmlns:mx='library://ns.adobe.com/flex/mx'>\n" +
	           "    <fx:Style>" + EOL +
		       "        ";
	}
			
    private String getPostfix()
    {
    	return EOL +
		       "    </fx:Style>" + EOL +
		       "</d:Sprite>";
    }
	

	public ICSSDocument getCSSNodeBase(String code) {
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

        IMXMLFileNode fileNode = super.getMXMLFileNode(getPrefix() + code + getPostfix());
		IMXMLStyleNode styleNode = (IMXMLStyleNode) findFirstDescendantOfType(fileNode, IMXMLStyleNode.class);
		
		assertNotNull("styleNode", styleNode );		
					
		return styleNode.getCSSDocument(problems);
	}
	
	protected List<ICSSProperty> getCSSProperties(String code) {
		ICSSDocument doc = getCSSNodeBase(code);
		
		ImmutableList<ICSSRule> rules = doc.getRules();
		List<ICSSProperty> properties = new ArrayList<ICSSProperty>();
		for (ICSSRule icssRule : rules) {
			properties.addAll( icssRule.getProperties() );
		}
		assertThat("properties", properties,  not( (List<ICSSProperty>) null) );

		return properties;
	}
	
	protected List<ICSSSelector> getCSSSelectors(String code) {
		ImmutableList<ICSSRule> rules =  getCSSNodeBase( code ).getRules();
		assertThat("rules", rules,  not( (ImmutableList<ICSSRule>) null) );
		List<ICSSSelector> selectors = new ArrayList<ICSSSelector>();
		for (ICSSRule icssRule : rules) {
			selectors.addAll( icssRule.getSelectorGroup() );
		}
		assertThat("selectors", selectors,  not( (List<ICSSSelector>) null) );
		return selectors;
	}

}
