# fallout-terminal-solver

## What?

Designed to facilitate solving [terminals in the Fallout series](https://fallout.fandom.com/wiki/Hacking).
Specifically, I play *Fallout: New Vegas*.

Single-class Java program with a text file as input.
Takes word lists, all of the same length, and determines which word has the most letters in common with as many of the other words as possible.
When you give it the output from the terminal (how many letters in common with the actual password your guess has),
it narrows down the provided wordlist to just the words matching that output,
and re-performs the same calculation.

## Why?

Existing Fallout terminal solvers are good at telling you which word to guess in order to most efficiently guess the solution with no other knowledge.

It is my observation that while my algorithm is not the most efficient way to solve any arbitrary puzzle constructed under these rules - 
in fact, it is notably inefficient in doing so -
in *Fallout: New Vegas*, the solutions are usually words which have a lot of letters in common with the other potential passwords.

For that reason, rather than a tool that identifies the quickest path to a potential solution, I wanted a tool which would identify
the most likely solution, assuming that the solution is more likely to be a word which has more letters in common with the other words in the password list.

## How?

Nothing fancy. Counting letters in common, summing, maxing.

## Who?

[My LinkedIn](https://www.linkedin.com/in/david-mccourt-66537539)
