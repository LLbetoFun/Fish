package com.fun.inject.transform;

import java.security.ProtectionDomain;

public interface IClassTransformer {
     byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer);
}
