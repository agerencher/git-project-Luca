# git-project-Luca
1.  Did you code stage() / how well does it work?
Yes. stage() is coded and works for all kinds of scenarios. Adding driectories, files, files in directories. As well as editing files.

2.  Did you code commit() / how well does it work?
Yes, commit() is coded and uses 2 helper methods: createTree() and makeCommit(). These both work when tested, no matter the scenario. It makes sure duplicate files don't appear when a file is editied.
3. Did you do checkout / how well does it work?
Nope.
4. What bugs did find / which of em did you fix?
One bug was when a file was edited, 2 of them will appear in the tree, so I had to remove the original one by searching through the roottree and not copy it to the new tree. 

How to use Interface:

Initialize a repo:
Git repo = new Git();

Create File:
File fileName = new File(path);
        FOR FILE            FOR DIRECTORY
fileName.createNewFile() OR fileName.mkdir(); 

Stage Files:
repo.stage(fileName);

Commit Changes:
repo.commit(author, message)


