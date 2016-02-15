package com.gentics.mesh.core.data.schema;

import com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeOperation;

/**
 * Change entry which contains information for a field to be added to the schema.
 */
public interface AddFieldChange extends SchemaFieldChange {

	public static final SchemaChangeOperation OPERATION = SchemaChangeOperation.ADDFIELD;

	/**
	 * Set the type of the field that should be added.
	 * 
	 * @param type
	 * @return Fluent API
	 */
	AddFieldChange setType(String type);

	/**
	 * Returns the type of the field that should be added.
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Return the list type field value.
	 * 
	 * @return The list type is null for non list fields
	 */
	String getListType();

	/**
	 * Set the list type field value.
	 * 
	 * @param type
	 */
	void setListType(String type);

}
