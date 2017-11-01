# Test projects

The unit tests for this project 
automatically scan this directory
for folders and then
process the .conf files
and compare the output
to the expected.yaml file.

To create a new test
it is suggested that you 
copy an existing test
and modify it to suit your needs.

The input file the unit test
will read in is `envionment.conf`.
The other .conf files are 
imported using directives
in this file.
PureConfig automatically resolves this.

Comments (lines starting with a '#')
in the `expected.yaml` are ignored.