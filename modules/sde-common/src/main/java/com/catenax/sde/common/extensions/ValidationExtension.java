package com.catenax.sde.common.extensions;

public interface ValidationExtension {
	
	default boolean validateRecord() {
		return false;
	}

}
