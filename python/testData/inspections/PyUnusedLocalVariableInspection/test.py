def foo1():
  <warning descr="Local variable 'aaa' value is not used">aaa</warning> = 1 #fail
  <warning descr="Local variable 'aaa' value is not used">aaa</warning> = 2 #fail

  bbb = 1 #pass
  <warning descr="Local variable 'bbb' value is not used">bbb</warning> += 2 #fail

def bar(<warning descr="Parameter 'self' value is not used">self</warning>): #fail
  print("Foo")

def baz(<warning descr="Parameter 'a' value is not used">a</warning>): #fail
  a = 12
  print(a)

def boo():
  <warning descr="Local variable 'k' value is not used">k</warning> = 1 #fail
  [k for k in [1,3] if True]

  <warning descr="Local variable 'i' value is not used">i</warning> = 1 #fail
  for i in [1,2]:
    print(i)

  j = 1 #pass
  for j in [-2, -1]:
    print(j)
  print (j)

class A:
  def foo(self): #pass
    pass

def baa():
    foo.bar = 123 #pass

def bla(x): #pass
  def _bar():
    print x
  return _bar

def main():
  foo = "foo" #pass
  bar = "bar" #pass
  baz = "baz" #pass
  print "%(foo)s=%(bar)s" % locals()


def bar(arg): #pass
  foo = 1 #pass
  def <warning descr="Local function 'test' is not used">test</warning>(): #fail
    print arg
    return foo #pass

class FooBar:
    @classmethod
    def foo(cls): #pass
        pass
        args[0] = 123 # pass

def bzzz():
    for _ in xrange(1000): # pass
        pass

def do_something(local_var):
    global global_var
    global_var = local_var # pass

def srange(n):
    return {i for i in range(n)}

def test_func():
    return {(lambda i=py1208: i) for py1208 in range(5)}


def foo():
    status = None #pass
    try:
        status = open('/proc/self/status', 'r')
    finally:
        if status is not None:
            status.close()

foo  = lambda <warning descr="Parameter 'x' value is not used">x</warning>: False


class MySuperClass:
    def shazam(self, param1, param2): pass

class MySubClass(MySuperClass):
    def shazam(self, param1, param2): pass

def test_func():
    items = {(lambda i=i: i) for i in range(5)} #pass
    return {x() for x in items}

class MyType(type):
    def __new__(cls, name, bases, attrs):  #pass
        if name.startswith('None'):
            return None
        return super(MyType, cls).__new__(cls, name, bases, newattrs)

    def foo(cls): #fail
        pass

def test():
    d = dict()
    v = 1 #pass
    try:
        v = d['key']
    except KeyError:
        v = 2
    finally:
        print v

def foo(<warning descr="Parameter 'a' value is not used">a = 123</warning>): # fail Do not use getText() as parameter name
    pass

def loopie():
    for x in range(5): pass
    for y in xrange(10): pass

def locals_inside():
    now = datetime.datetime.now() # pass
    do_smth_with(locals())

class Stat(object):
  @staticmethod
  def woof(<warning descr="Parameter 'dog' value is not used">dog="bark"</warning>): # fail
    pass

class A:
    def __init__(self, *args): #pass
        self.args = []
        for a in args:
            self.args.append(a)


# PY-2574
def f():
    # x in "for" is actually used in "if"
    return [0 for x in range(10) if x] #pass


# PY-3031
def f():
    # n doesn't leak from the generator expression, so this n is used
    n = 1 #pass
    expr = (n for n in xrange(10))
    print n
    return expr


# PY-3118
def f(x):
    # Both y values in "if" can be used later in g, so the first one shouldn't be marked as unused
    if x:
        y = 0 #pass
    else:
        y = 1 #pass
    def g():
        return y
    return g


# PY-3076
def f():
    # Both x values could be used inside g (because there is no inter-procedural CFG)
    x = 'foo' #pass
    x = 'bar' #pass
    def g():
        return x
    x = 'baz' #pass
    return g


# PY-2418
def f(x):
    # The list shouldn't be marked as unused
    if isinstance(x, [tuple, list]): #pass
        pass


# PY-3550
def f():
    z = 2 #pass
    def g(z=z):
        return z
    return g


# PY-4151
def f(g):
    x = 1 #pass
    try:
        x = g()
    except Exception:
        pass
    else:
        pass
    print(x)


# PY-4154
def a1():
    <warning descr="Local variable 'a' value is not used">a</warning> = 1 #fail
    try:
        a = 2
    except Exception:
        a = 3
    print(a)


# PY-4157
def f(g):
    x = 1 #pass
    try:
        pass
    except Exception:
        pass
    else:
        x = g()
    print(x)


# PY-4147
def f(x, y, z):
    class C:
        foo = x #pass
        def h(self):
            return z
    def g():
        return y
    return C, g


# PY-4378
def f(c):
    try:
        x = c['foo']
    except KeyError:
        if c:
            x = 42 #pass
        else:
            raise
    except Exception:
        raise
    return x


# PY-5755
def test_same_named_variable_inside_class():
    a = 1 #pass
    class C:
        def a(self):
            print(a)
    return C
