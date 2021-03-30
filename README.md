
# Investment library for scripting and use in projects

Before we can start I add the prototypical investment disclaimer:

> I am neither a finance professional nor do I guarantee that his code works 
> correctly. I also make no investment decisions for you. To sum it up, your investment decisions
> are your responsibility.

This library provides you with functionality to answer some of the questions that arise when planing an investment:

  - How much do I have to invest to reach a certain goal, taking taxes and inflation into account?
  - Is my investment goal achievable at all?
  - How do I have to invest so a bear market isn't killing my investment goals (expected return)?
  - And more ...

It is structured in a way that it is easily extended with new functionality like different taxation
strategies, order flow behaviour, asset value development, etc. That means, while it has limited out of the
box functionality now (support for stocks and bonds and therefore ETFs based upon them) it can always we be
extended.

To provide easy access to its functionality especially for scripts this repo ships with a Docker image with
a ready to use environment. No Scala, SBT, [ammonite] required on your machine. Just spin up a container
and start coding. For more information have a look at [How to install and use it with a Docker based setup](#how-to-install-and-use-it-with-a-docker-based-setup).

## ToC
In the following section you will find How-Tos to install and use this library. For specific use cases
look into the `how-to` directory.

## How to install and use this library
This section describes how to make this library available to you in your scripts and projects.

### How to install and use it with a local Scala/SBT setup
To load this library as dependency into local projects or to include it in Scala scripts you have to run
the following command in this projects directory:

```bash
cd /path/to/investment-library
sbt "publishLocal"
```

You can find the current version (`0.0.0`) in the last lines of the output.

### How to use it with local Ammonite
When you have [ammonite] installed on your machine start it:

```bash
amm
```

Now you can import this library with the following line:

```scala
import $ivy.`com.github.pheymann::investment-library:0.0.0`
```

It requires that you published the lirbary first.

### How to install and use it with a Docker based setup
When you don't have Scala, SBT and [ammonite] on your machine you can build a Docker image:

```
docker build --tag investment:v0 .
```

To open an environment run the following Docker command:

```bash
docker run --rm --name investment -it -v /path/to/my/scripts:/investment/scripts investment:v0
```

In this environment you can now use [ammonite] as described for the local case.

```bash
amm
```

```scala
# inside ammonite session
import $ivy.`com.github.pheymann::investment-library:0.0.0`
```

### How to compile and run Scala Scripts
Simply include the same import as header in your [Scala Script] (here `test.sc`):

```scala
import $ivy.`com.github.pheymann::investment-library:0.0.0`
```

Then `watch` that file:

```bash
amm --watch test.sc
```

[ammonite]: https://ammonite.io/#Ammonite-REPL
[Scala Script]: https://ammonite.io/#ScalaScripts

## Troubleshooting
### Ammonite: Libray cannot be found
When you see the following message while running [ammonite] *locally* (meaning: not Docker) you most likely forgot to publish this library. To do
so follow [this description](how-to-publish-this-library-locally).

If you run [ammonite] from a Docker image make also sure you publish this library, but also check that you mount `.ivy2` into the
correct directory in that container. The error message should tell you where Ivy is looking for it:

*Docker ammonite output*:
```bash
not found: /root/.ivy2/local/com.github.pheymann/investment-library_2.13/0.0.0/ivys/ivy.xml
```