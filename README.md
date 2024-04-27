# Gnife [naɪf]
[![build](https://github.com/chrovis/gnife/actions/workflows/build.yml/badge.svg)](https://github.com/chrovis/gnife/actions/workflows/build.yml)

Gnife is a CLI tool for manipulating genomic files and data.

## Installation

### Manual Install

Gnife requires you have installed Java. A binary are available on the
[releases](https://github.com/chrovis/gnife/releases) page.

```sh
curl -sSL https://github.com/chrovis/gnife/releases/download/0.1.3/gnife -o gnife
chmod +x gnife
mv gnife [/your/PATH/dir/]
```

### Homebrew

You can use [Homebrew](https://brew.sh/) on MacOS.

```sh
brew install xcoo/formulae/gnife
```

### Build

To build Gnife manually, you must setup Clojure (and Java) in advance.

```sh
clojure -T:build bin
cp target/gnife [/your/PATH/dir/]
```

## Getting Started

Each Gnife command can be called by `gnife [type] [command]`.

```console
$ gnife hgvs repair "c.123_124GC>AA"
c.123_124delGCinsAA
```

## Command List

Gnife commands are grouped by data type. `gnife --tree` lists all commands in a
tree-like format.

```console
$ gnife --tree
sequence
  dict   Create a sequence dictionary for a reference sequence
  faidx  Index a reference sequence in the FASTA format

sam
  view       Extract/print all or sub alignments in SAM or BAM format
  convert    Convert file format based on the file extension
  normalize  Normalize references of alignments
  sort       Sort alignments by leftmost coordinates
  index      Index sorted alignment for fast random access
  pileup     Generate pileup for the BAM file
  level      Analyze a BAM file and add level information of alignments

vcf
  liftover  Convert genomic coordinates in a VCF file between assemblies

variant
  liftover  Convert a genomic coordinate between assemblies
  to-hgvs   Convert a VCF-style variant into HGVS

hgvs
  format      Format HGVS with a specified style
  repair      Repair an invalid HGVS
  to-variant  Convert a HGVS into VCF-style variants
```

`gnife [type] [command] --help` to display detailed usage of each command.

## JVM Options

To pass extra arguments to the JVM, set the `GNIFE_JVM_OPTS` environment
variable.

```sh
export GNIFE_JVM_OPTS="-XX:TieredStopAtLevel=1 -Xmx4g"
```

## Test

To run tests,

- `clojure -X:test` for basic tests, and
- `clojure -X:test:slow-test` for slow tests with remote resources.

## License

Copyright 2024 [Xcoo, Inc.](https://xcoo.jp/)

Licensed under the [Apache License, Version 2.0](LICENSE).
