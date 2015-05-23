package com.ayld.test.src;

import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.Set;

public class Test {
	public static void main(String[] args) {
		final Optional<Integer> integer = Optional.of(10);
		System.out.println(integer.get());

		final Set<Integer> set = Sets.newHashSet();
		set.add(10);
		System.out.println(set);
	}
}
