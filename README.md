Welcome to the RepIR repo. 

The Repository for Information Retrieval Experiments (RepIR) supports retrieval/analysis tasks on large collections using Hadoop. There is a Wiki page on each repository with more information what is is and how to get started. For navigating in this repository it may be good to know that it consists of 3 parts:
- RepIRtools: RepIR needs these, but unless you are digging really deep, you probably don't want to look too closely at these, unless you are creating a custom implementation such as an RepIR Record type file or a ByteRegex. In that case look in the JavaDocs for info.
- RepIR: This is the core library for working with the Repository, Stored Features, Test Sets, Retrieval Models, Score Functions, Collectors, etc. These are not applications, but rather tools you can use in your Java programs, for instance to access the repository or to create a custom component.
- RepIRapps: These are some custom apps that use RepIR. BuildVocabulary and BuildRepository are general processes to extract features from a collection and store these in a new Repository. The Retrieve folder contains several retrieval apps varying from running a MapReduced test set to running a local single query from the command line.

Additional projects such as RepIRProximity are research projects. You don't need this to use RepIR, only if you are specifically looking for this.

RepIR was used for the study described in Vuurens, J. B., & de Vries, A. P. (2014). Distance matters! Cumulative proximity expansions for ranking documents. Information Retrieval, 1-27. For this study, release 0.23 (available in Maven Central repo) was used, but you can also use the latest snapshot from github. To use it, you need RepIRProx (the master pom). Change pom.xml in RepIRProx to configure the Hadoop version being used, and then compile. When solving dependencies, the pom will download the correct versions of RepIR, RepIRTools, RepIRApps and RepIRProximity.
