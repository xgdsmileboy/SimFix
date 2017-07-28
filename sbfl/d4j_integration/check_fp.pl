#!/usr/bin/env perl

=pod

=head1 NAME

check_fp.pl -- review a failing test suite and check whether it is a false positive.

=head1 SYNOPSIS

  check_fp.pl -s test_suite -t trigger_tests

=head1 OPTIONS

=over 4

=item -s F<test_suite>

The test suite archive that contains the sources of the failing, generated test suite.

=item -t F<trigger_tests>

The file that lists all triggering tests of the failing, generated test suite.

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
use Utils;

#
# Process arguments and issue usage message if necessary.
#
my %cmd_opts;
getopts('s:t:', \%cmd_opts) or pod2usage(1);

my $SUITE    = $cmd_opts{s};
my $TRIGGERS = $cmd_opts{t};
pod2usage(1) unless defined $SUITE and defined $TRIGGERS;

my $OUT_DIR  = $ENV{"D4J_DB_DIR"} // "./result_db";

=pod

=head1 EDITOR

The default editor (merge tool) used to visualize the patch and trigger files is meld.
A different editor can be set via the environment variable D4J_EDITOR.

=cut
my $EDITOR = $ENV{"D4J_EDITOR"} // "meld";

=pod

=head1 USER NAME

The default user name of the analyst is determined by running C<whoami>.
A different user name for the analyst can be set via the environment variable D4J_USER.

=cut
my $USER = $ENV{"D4J_USER"};
unless (defined $USER) {
    $USER = `whoami`;
    chomp $USER;
}

$SUITE =~ /^(.*\/)?([^-]+)-(\d+[bf])-([^\.]+)(\.(\d+))?\.tar\.bz2$/
        or die "Invalid name of test suite archive: $SUITE";
my $pid = $2;
my $vid = $3;
my $src = $4;
my $tid = ($6 or "1");
# Obtain bug id
my $bid=Utils::check_vid($vid)->{bid};

# Check file with triggering tests
-f $TRIGGERS or die "File with triggering tests not found: $TRIGGERS";

# Set up temporary directory
my $TMP_DIR = Utils::get_tmp_dir();
system("mkdir -p $TMP_DIR");

# Set up project
my $project = Project::create_project($pid);
$project->{prog_root} = $TMP_DIR;

# Directory that holds all patches for the given project ID
my $patch_dir = "$SCRIPT_DIR/projects/$pid/patches";
-e $patch_dir or die "Cannot read patch directory: $patch_dir";

# Checkout project version and apply patch for better visualization
my $src_patch = "$patch_dir/${bid}.src.patch";
my $src_path = $project->src_dir("${bid}f");
$project->checkout_vid("${bid}f");
$project->apply_patch($TMP_DIR, $src_patch) or die "Cannot apply patch";

# Extract test suite
Utils::extract_test_suite($SUITE, "$TMP_DIR/gen-tests") or die;

# Provide test suite and trigger file
system("grep -A1 '\\---' $TRIGGERS > $TMP_DIR/triggers_summary");
system("cp $TRIGGERS $TMP_DIR/triggers_details");
system("cd $TMP_DIR && git add gen-tests triggers_summary triggers_details");

# Show diff, test suite, and triggers in configured editor
system("$EDITOR $TMP_DIR");

# Remove temporary directory
system("rm -rf $TMP_DIR");
