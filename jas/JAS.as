package jas {

  import flash.utils.ByteArray;

  public class JAS {

    public static function initByteArray(...args) : ByteArray
    {
      var ba:ByteArray = new ByteArray();
      for each (var v:int in args)
        ba.writeByte(v);
      ba.position = 0;
      return ba;
    }

    public static function newByteArray(length:int) : ByteArray
    {
      var ba:ByteArray = new ByteArray();
      ba.length = length;
      return ba;
    }

    public static function assert(...args) : void
    {
      trace("assert called...");
    }
  }
}
