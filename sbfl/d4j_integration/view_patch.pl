#!/usr/bin/env perl

=pod

=head1 NAME

view_patch.pl -- View a patch in a visual diff editor.

=head1 SYNOPSIS

view_patch.pl -p project_id -b bug_id

=head1 OPTIONS

=over 4

=item -p C<project_id>

The id of the project for which the patch should be displayed.

=item -b C<bug_id>

The id of the bug for which the patch should be displayed.

=back

=cut

use warnings;
use strict;
use FindBin;
use File::Basename;
use Cwd qw(abs_path);
use Getopt::Std;
use Pod::Usage;

my $D4J_CORE;
BEGIN{
    unless (defined $ENV{D4J_HOME}) {
        die "D4J_HOME not set!\n";
    }
    $D4J_CORE = "$ENV{D4J_HOME}/framework/core";
}
use lib abs_path("$D4J_CORE");
use Constants;
use Project;

#
# Process arguments and issue usage message if necessary.
#
my %cmd_opts;
getopts('p:b:', \%cmd_opts) or pod2usage(1);

pod2usage(1) unless defined $cmd_opts{p} and defined $cmd_opts{b};

=pod

=head1 EDITOR

The default editor (merge tool) used to visualize patches is meld.
A different editor can be set via the environment variable D4J_EDITOR.

=cut
my $EDITOR = $ENV{"D4J_EDITOR"} // "meld";

my $PID      = $cmd_opts{p};
my $BID      = $cmd_opts{b};
# Check format of target version id
$BID =~ /^(\d+)$/ or die "Wrong bug id format: $BID -- expected: (\\d+)!";

my $patch_dir = "$SCRIPT_DIR/projects/$PID/patches";
-e $patch_dir or die "Cannot read patch directory: $patch_dir";

my $src_patch = "$patch_dir/${BID}.src.patch";

my $TMP_DIR = Utils::get_tmp_dir();
system("mkdir -p $TMP_DIR");

# Set up project
my $project = Project::create_project($PID);
$project->{prog_root} = $TMP_DIR;

my $src_path = $project->src_dir("${BID}f");
$project->checkout_vid("${BID}f");
$project->apply_patch($TMP_DIR, $src_patch) or die "Cannot apply patch";

# View patch with configured editor
system("$EDITOR $TMP_DIR");

# Remove temporary directory
system("rm -rf $TMP_DIR");
