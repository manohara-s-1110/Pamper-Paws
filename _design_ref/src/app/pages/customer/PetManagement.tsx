import { useState } from 'react';
import { PawPrint, Plus, Pencil, Image as ImageIcon } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '../../components/ui/dialog';
import { getPetsByCustomerId } from '../../lib/data';
import { toast } from 'sonner';

export function PetManagement() {
  const currentCustomerId = 'c1'; // Mock current user
  const [pets, setPets] = useState(getPetsByCustomerId(currentCustomerId));
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingPet, setEditingPet] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const name = formData.get('name') as string;
    const breed = formData.get('breed') as string;
    const age = parseInt(formData.get('age') as string);

    if (editingPet) {
      toast.success(`${name}'s information has been updated!`);
    } else {
      toast.success(`${name} has been added to your pets!`);
    }

    setIsDialogOpen(false);
    setEditingPet(null);
  };

  const openEditDialog = (petId: string) => {
    setEditingPet(petId);
    setIsDialogOpen(true);
  };

  const openAddDialog = () => {
    setEditingPet(null);
    setIsDialogOpen(true);
  };

  const currentPet = editingPet ? pets.find(p => p.id === editingPet) : null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="mb-2">My Pets</h1>
          <p className="text-muted-foreground">Manage your beloved pets' information</p>
        </div>
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={openAddDialog}>
              <Plus className="h-4 w-4 mr-2" />
              Add Pet
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>{editingPet ? 'Edit Pet' : 'Add New Pet'}</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Pet Name</Label>
                  <div className="relative">
                    <PawPrint className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                      id="name"
                      name="name"
                      placeholder="Enter pet name"
                      className="pl-10"
                      defaultValue={currentPet?.name}
                      required
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="breed">Breed</Label>
                  <Input
                    id="breed"
                    name="breed"
                    placeholder="Enter breed"
                    defaultValue={currentPet?.breed}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="age">Age (years)</Label>
                  <Input
                    id="age"
                    name="age"
                    type="number"
                    min="0"
                    max="30"
                    placeholder="Enter age"
                    defaultValue={currentPet?.age}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="image">Pet Image</Label>
                  <div className="flex items-center gap-3">
                    <div className="flex-1">
                      <Input
                        id="image"
                        name="image"
                        type="file"
                        accept="image/*"
                        className="cursor-pointer"
                      />
                    </div>
                    <ImageIcon className="h-5 w-5 text-muted-foreground" />
                  </div>
                  <p className="text-xs text-muted-foreground">Upload a photo of your pet</p>
                </div>
              </div>

              <div className="flex gap-3 justify-end">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setIsDialogOpen(false);
                    setEditingPet(null);
                  }}
                >
                  Cancel
                </Button>
                <Button type="submit">
                  {editingPet ? 'Update Pet' : 'Add Pet'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {pets.map((pet) => (
          <Card key={pet.id} className="overflow-hidden group hover:shadow-lg transition-shadow">
            <div className="aspect-square relative overflow-hidden bg-muted">
              <img
                src={pet.image}
                alt={pet.name}
                className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300"
              />
            </div>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <PawPrint className="h-5 w-5 text-primary" />
                  {pet.name}
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => openEditDialog(pet.id)}
                  className="opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  <Pencil className="h-4 w-4" />
                </Button>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <dl className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <dt className="text-muted-foreground">Breed:</dt>
                  <dd className="font-medium">{pet.breed}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-muted-foreground">Age:</dt>
                  <dd className="font-medium">{pet.age} {pet.age === 1 ? 'year' : 'years'}</dd>
                </div>
              </dl>
            </CardContent>
          </Card>
        ))}
      </div>

      {pets.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <PawPrint className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="mb-2">No pets yet</h3>
            <p className="text-muted-foreground mb-6">Add your first pet to get started</p>
            <Button onClick={openAddDialog}>
              <Plus className="h-4 w-4 mr-2" />
              Add Your First Pet
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
