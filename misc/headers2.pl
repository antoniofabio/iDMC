#!/usr/bin/perl -i

# first argument is the header file
# second one the file to be changed
# use -i.orig above to make backup files

use strict;
use warnings;

my $copyright_file = $ARGV[0];

open(H, "< " . $copyright_file) || die("can't open " . $copyright_file . ": $!");
my @header = <H>;

shift(@ARGV);

if (eof()) {
    die "EOF found\n";
}

# skip initial empty lines
while (<>)
{
#	print $_;
    if (/^\s*$/) {
	next;
    }
    last;
}

if (eof()) {
    die "EOF found\n";
}

# look for comment beginning
if (!/^\s*\/\*/) {
    die "comment not found\n";
}

# look for comment end
while (<>) {
    if (/^\s*\*\/$/) {
	last;
    }
    next;
}

if (eof()) {
    die "EOF found\n";
}

# output header
print @header;

# output remaining lines
while (<>)
{
    print;
}
