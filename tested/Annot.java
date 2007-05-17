public @interface Annot
{
  String name() default "noname";
  int value()   default 42;
}
