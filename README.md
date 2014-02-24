Welcome to the IREF repository. 

The Information Retrieval Experiments Framework supports retrieval/analysis tasks on large collections using Hadoop. There is a Wiki page on each repository with more information what is is and how to get started. For navigating in this repository it may be good to know that it consists of 3 parts:
- IREFtools: IREF needs these, but unless you are digging really deep, you probably don't want to look too closely at these, unless you are creating a custom implementation such as an IREF Record type file or a ByteRegex. In that case look in the JavaDocs for info.
- IREF: This is the core library for working with the Repository, Stored Features, Test Sets, Retrieval Models, Score Functions, Collectors, etc. These are not applications, but rather tools you can use in your Java programs, for instance to access the repository or to create a custom component.
- IREFapps: These are some custom apps that use IREF. BuildVocabulary and BuildRepository are general processes to extract features from a collection and store these in a new Repository. The Retrieve folder contains several retrieval apps varying from running a MapReduced test set to running a local single query from the command line.

Additional repositories such as IREFproximitymodel are research projects. You don't need this to run IREF, only if you are specifically looking for this.
