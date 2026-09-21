# Keeps obfuscated stack traces decodable: the line table survives, and the source file name is
# replaced by a constant so it leaks nothing. Traces are read back with mapping.txt via retrace.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
