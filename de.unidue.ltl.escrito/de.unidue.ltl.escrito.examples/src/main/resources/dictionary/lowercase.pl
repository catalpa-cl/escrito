#!/usr/bin/perl
use strict;

my $infile = $ARGV[0];
my $outfile = $infile;
$outfile=~s/\.txt/_lc.txt/;
open (IN, $infile) or die "wäh!";
open (OUT, ">$outfile") or die "wäh!!";
while (my $line = <IN>){
    print OUT lc $line;
}
close IN;
close OUT;
