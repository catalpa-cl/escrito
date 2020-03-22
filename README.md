# ESCRITO - Educational SCoRIng TOolkit

ESCRITO is a toolkit for scoring student writings using NLP techniques.
It addresses two main user groups: teachers and NLP researchers.

Teachers can use a high-level API in the teacher mode to assemble scoring pipelines easily.
NLP researchers can use the developer mode to access a low-level API, which not only makes available a number of pre-implemented components, but also allows the user to integrate their own readers, preprocessing components, or feature extractors.

In this way, the toolkit provides a ready-made testbed for applying the latest developments from NLP areas like text similarity, paraphrase detection, textual entailment, and argument mining within the highly challenging task of educational scoring and feedback. At the same time, it allows teachers to apply cutting-edge technology in the classroom.

## Features

* easy access to existing datasets
* integrated preprocessing
* state-of-the-art feature extraction
* shallow and deep learning setups
* evaluation reports and visualization

## Publications

> Torsten Zesch and Andrea Horbach. ESCRITO-An NLP-Enhanced Educational Scoring Toolkit. In Proceedings of the Eleventh International Conference on Language Resources and Evaluation (LREC-2018), 2018. [pdf](https://www.aclweb.org/anthology/L18-1365.pdf)

If you use ESCRITO, please cite:
```
@inproceedings{zesch2018escrito,
  title={ESCRITO-An NLP-Enhanced Educational Scoring Toolkit},
  author={Zesch, Torsten and Horbach, Andrea},
  booktitle={Proceedings of the Eleventh International Conference on Language Resources and Evaluation (LREC-2018)},
  year={2018},
  url= {https://www.aclweb.org/anthology/L18-1365}
}
```

## Usage

### Prerequisites

The project uses Java 1.8.
You need to set a DKPRO_HOME variable as described in the [DKPro documentation](https://zoidberg.ukp.informatik.tu-darmstadt.de/jenkins/job/DKPro%20TC%20Documentation%20(GitHub)/org.dkpro.tc%24dkpro-tc-doc/doclinks/1/#QuickStart)


### First Steps
- Import ESCRITO as a maven project in eclipse
- Try out the examples in the examples module




