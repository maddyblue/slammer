ant tasks supported here are for creating the installer package bundles (i.e., earthquake sets, program sets, source set) and combining them together into an installer.

The filelist target (ant filelist) will bundle (tar) and compress (bzip2) the package bundles and print their file and disk sizes. These sizes must then manually be entered into installer/slammer/install.props and installer/srm/install.props.

The installer and srminstaller targets (ant installer, ant srminstaller) will compile the installation java files and build jar files containing the installer code and compressed package bundles.

To recap, the steps to building the installers are:
1) ant filelist
2) Copy property information printed from the above step into the props file. This information generally does not change much.
3) ant installer
4) ant srminstaller
