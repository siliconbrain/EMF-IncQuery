package org.eclipse.viatra2.emf.incquery.queryexplorer.content.detail;

/**
 * This class represents a single row in the table viewer and is associated to a single 
 * ObservableValue and it's value.
 * 
 * @author Tamas Szabo
 *
 */
public class DetailElement {

	private String key;
	private String value;

	public DetailElement(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[key="+key+",value="+value+"]";
	}
}
