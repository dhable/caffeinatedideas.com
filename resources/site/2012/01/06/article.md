I hate threads.

More accurately, I hate the abstraction the industry has settled on for trying
to do work in parallel. When the computer scientists sat down and created the
first threading API, they damned all future computer scientists to the wonderful
joy of debugging shared memory states and complex issues revolving around an
API that you can't help but shoot yourself in the foot with. For instance, most
systems let you start a thread but then leave actually halting a thread to be this
complex, tangled mess of state signaling, join methods (which might not actually
join in the end) and a boat load of blocking issues.

As an example, we're currently working on some Python code where our controlling
process, we'll call it the Manager, creates a separate thread used to execute a
sync action, which we'll call the SyncEngine. So it's fairly easy to make SyncEngine
a thread, we'll use use inheritance like so:

```python
from threading import Thread

class SyncEngine(Thread):
   def run(self):
      while True:
         # yadda, yadda, yadda
```

Now from anywhere in the Manager, we can create and start that thread:

```python
engine = SyncEngine()
engine.start() # And we're off!
```

So starting this new resource which is going to consume CPU cycles and get work done
was quite simple. How do we stop it? Well, reading the
[API docs for the Python threads](http://docs.python.org/py3k/library/threading.html#thread-objects),
we need to implement this in the SyncEngine run method.
Let's return to our SyncEngine definition then and add the necessary code to
shut it down.

```python
from threading import Thread

class SyncEngine(Thread):
   def __init__(self, *args, **kwargs):
      super().__init__(*args, **kwargs)
      self.running = False

   def stop(self):
      self.running = False

   def run(self):
      self.running = True
      while self.running:
         # yadda, yadda, yadda
```

This works for very simple cases but the SyncEngine ends up calling into various
other modules with a huge amount of code. What if that code decides to block
on some I/O that's taking a long time. The only way to shut down the SyncEngine
is to set running to False and wait on the join call from the Thread API. Furthermore,
what if I wanted to introduce another sync thread? I'd have to rework the design,
lock shared objects and get into all the details from the SyncEngine object down
to make sure my new algorithm is actually designed for two threads. Then we find
out three threads would be ideal.

In all, the Thread abstraction has created a mess. It doesn't protect my code from
any changes that might happen with the number of running threads and forces me to
think about all the bad things that can happen when multiple threads start competing
for resources. The solution to this might be for platform developers to stop making
us think that we need to worry and design our programs in terms of Threads. Instead,
we could think about each little "task" we need to perform and then add them to the
"plan", which can be as simple as a list of tasks to execute.

Python makes this implementation easy since everything is an object. If I define a
function, I can pass around the reference to that function and then call it  later on:

```python
def myfunc():
   print("Doing something useful.")

def exec_func(callable):
   callable()

# I can now use the reference to my function and
# pass it to a different function
exec_func(myfunc)
```

This simple trick then allows me to implement a task queue with callable items in
it that could then be serviced by a single Thread.

```python
import queue, threading

class TaskExecutor(threading.Thread):
   def __init__(self, *args, **kwargs):
      super().__init__(*args, **kwargs)
      self._task_queue = queue.Queue()
      self._queue_lock = threading.Lock()

   def schedule_for_execution(self, callable_item):
      with self._queue_lock:
         self._task_queue.put(callable_item)

   def run(self):
      while True:
         next_callable_item = None
         while not next_callable_item:
            with self._queue_lock:
               try:
                  next_callable_item = self._task_queue.get_nowait()
               except queue.Empty:
                  pass # try again if it's empty
         next_callable_item()
```

My simple example only uses a single thread to dispatch the tasks but one could
tune this to use some number of threads to perform the work. Incorporated into
a large framework, this breaks the idea that each developer needs to worry about
the various threading problems and can instead focus on getting the business logic
of each task correct. Also, a more complex implementation could store a tag along
with the items in the queue so one could resort the queue to move all tasks of a
particular type up to the front of the queue - a very useful trick for an
interactive UI application.

As long as devices, operating systems and frameworks keep the cost of creating
threads cheap, developers will be doomed to troubleshoot the same set of concurrency
issues over and over again. It's time to start looking for alternatives.