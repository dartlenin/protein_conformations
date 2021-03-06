% aligns model2 to model1
function result = alignModels(model1, model2)
model1Atoms = getModelAtomMatrix(model1);
model2Atoms = getModelAtomMatrix(model2);
[~, alignedModelAtoms] = procrustes(model1Atoms, model2Atoms, 'scaling', false, 'reflection', false);
alignedX = num2cell(alignedModelAtoms(:, 1));
alignedY = num2cell(alignedModelAtoms(:, 2));
alignedZ = num2cell(alignedModelAtoms(:, 3));
result = model2;
[result.Atom.X] = alignedX{:};
[result.Atom.Y] = alignedY{:};
[result.Atom.Z] = alignedZ{:};